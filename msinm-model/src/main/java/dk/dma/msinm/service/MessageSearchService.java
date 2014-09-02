/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.msinm.service;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Shape;
import dk.dma.msinm.common.MsiNmApp;
import dk.dma.msinm.common.db.PredicateHelper;
import dk.dma.msinm.common.model.DataFilter;
import dk.dma.msinm.common.settings.annotation.Setting;
import dk.dma.msinm.common.util.TextUtils;
import dk.dma.msinm.lucene.AbstractLuceneIndex;
import dk.dma.msinm.model.Area;
import dk.dma.msinm.model.AreaDesc;
import dk.dma.msinm.model.Category;
import dk.dma.msinm.model.CategoryDesc;
import dk.dma.msinm.model.Chart;
import dk.dma.msinm.model.Location;
import dk.dma.msinm.model.LocationDesc;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.model.PointDesc;
import dk.dma.msinm.model.SeriesIdentifier;
import dk.dma.msinm.model.Type;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queries.ChainedFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.apache.lucene.spatial.query.SpatialArgs;
import org.apache.lucene.spatial.query.SpatialOperation;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Lucene search index for {@code Message} entities
 */
@Singleton
@Lock(LockType.READ)
@Startup
public class MessageSearchService extends AbstractLuceneIndex<Message> {

    final static String SEARCH_FIELD    = "message";
    final static String LOCATION_FIELD  = "location";
    final static String STATUS_FIELD    = "status";

    @Inject
    EntityManager em;

    @Inject
    Logger log;

    @Inject
    MsiNmApp app;

    @Inject
    MessageService messageService;

    @Inject
    @Setting(value = "messageIndexMaxMessageNo", defaultValue = "1000")
    Long maxMessageNo;

    @Inject
    @Setting(value = "messageIndexDir", defaultValue = "${user.home}/.msinm/msg-index", substituteSystemProperties = true)
    Path indexFolder;

    @Inject
    @Setting(value = "messageIndexSpatialLevels", defaultValue = "11")
    Long maxSpatialLevels;  // a value of 11 results in sub-meter precision for geohash

    @Inject
    @Setting(value = "messageIndexDeleteOnStartup", defaultValue = "true")
    Boolean deleteOnStartup;

    SpatialStrategy strategy;
    boolean allIndexed;

    /**
     * Initialize the index
     */
    @PostConstruct
    public void init() {
        // Create the lucene index directory
        if (!Files.exists(indexFolder)) {
            try {
                Files.createDirectories(indexFolder);
            } catch (IOException e) {
                log.error("Error creating index dir " + indexFolder, e);
            }
        }

        // Initialize the spatial strategy
        SpatialPrefixTree grid = new GeohashPrefixTree(SpatialContext.GEO, maxSpatialLevels.intValue());
        strategy = new RecursivePrefixTreeStrategy(grid, LOCATION_FIELD);

        // Check if we need to delete the old index on start-up
        if (deleteOnStartup) {
            try {
                deleteIndex();
            } catch (IOException e) {
                log.error("Failed re-creating the index on startup", e);
            }
        }
    }

    /**
     * Returns if all messages have been indexed
     * @return if all messages have been indexed
     */
    public boolean isAllIndexed() {
        return allIndexed;
    }

    /**
     * Returns the language specific language field
     * @param language the language
     * @return the language specific language field
     */
    private String searchField(String language) {
        return SEARCH_FIELD + "_" + app.getLanguage(language);
    }

    /**
     * Clean up Lucene index
     */
    @PreDestroy
    public void closeIndex() {
        closeReader();
    }

    /**
     * Called every minute to update the Lucene index
     */
    @Schedule(persistent=false, second="38", minute="*/1", hour="*", dayOfWeek="*", year="*")
    public int updateLuceneIndex() {
        return updateLuceneIndex(MAX_INDEX_COUNT, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Path getIndexFolder() {
        return indexFolder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Message> findUpdatedEntities(Date fromDate, int maxCount) {
        List<Message> messages = messageService.findUpdatedMessages(fromDate, maxCount);

        // The first time less that the maximum number of messages are found,
        // we flag that the indexing is complete
        if (messages.size() < maxCount) {
            allIndexed = true;
        }

        return messages;
    }

    /**
     * Adds a shape to the document
     * @param doc the Lucene document
     * @param shape the shape to add
     * @return the updated document
     */
    private Document addShapeSearchFields(Document doc, Shape shape) {
        for (IndexableField f : strategy.createIndexableFields(shape)) {
            doc.add(f);
        }
        doc.add(new StoredField(strategy.getFieldName(), shape.toString()));
        return doc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean shouldAddEntity(Message entity) {
        return true;
        //return entity.getStatus() != Status.DELETED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addEntityToDocument(Document doc, Message message) {

        // For each supported language, update a search field
        for (String language : app.getLanguages()) {
            String searchField = searchField(language);

            addPhraseSearchField(doc, searchField, message.getStatus());

            // Message series identifier
            addPhraseSearchField(doc, searchField, message.getSeriesIdentifier().getShortId()); // e.g. "DK-074-14"
            addPhraseSearchField(doc, searchField, message.getSeriesIdentifier().getFullId());  // e.g. "MSI-DK-074-14"
            addPhraseSearchField(doc, searchField, message.getSeriesIdentifier().getAuthority());
            addPhraseSearchField(doc, searchField, String.valueOf(message.getSeriesIdentifier().getYear()));
            if (message.getSeriesIdentifier().getNumber() != null) {
                addPhraseSearchField(doc, searchField, String.valueOf(message.getSeriesIdentifier().getNumber()));
            }

            // References
            message.getReferences().forEach(ref -> {
                addPhraseSearchField(doc, searchField, ref.getSeriesIdentifier().getShortId());
                addPhraseSearchField(doc, searchField, ref.getSeriesIdentifier().getFullId());
            });

            // Area
            for (Area area = message.getArea(); area != null; area = area.getParent()) {
                AreaDesc desc = area.getDesc(language);
                if (desc != null) {
                    addPhraseSearchField(doc, searchField, desc.getName());
                }
            }

            // Category
            message.getCategories().forEach(category -> {
                for (Category cat = category; cat != null; cat = cat.getParent()) {
                    CategoryDesc desc = cat.getDesc(language);
                    if (desc != null) {
                        addPhraseSearchField(doc, searchField, desc.getName());
                    }
                }
            });

            // Charts
            message.getCharts().forEach(chart -> {
                addPhraseSearchField(doc, searchField, chart.getChartNumber());
                addPhraseSearchField(doc, searchField, chart.getInternationalNumber());
            });

            // Horizontal datum
            addPhraseSearchField(doc, searchField, message.getHorizontalDatum());

            // Add language specific fields
            message.getDescs().forEach(desc -> {
                addPhraseSearchField(doc, searchField, desc.getTitle());
                addPhraseSearchField(doc, searchField, TextUtils.html2txt(desc.getDescription()));
                addPhraseSearchField(doc, searchField, desc.getNote());
                addPhraseSearchField(doc, searchField, desc.getOtherCategories());
                addPhraseSearchField(doc, searchField, desc.getVicinity());
                addPhraseSearchField(doc, searchField, desc.getPublication());
                addPhraseSearchField(doc, searchField, desc.getSource());
            });

            message.getLightsListNumbers()
                    .forEach(lightsListNumber -> addPhraseSearchField(doc, searchField, lightsListNumber));

            // Add descriptions for locations and points associated with the message.
            message.getLocations().forEach(location -> {
                LocationDesc locDesc = location.getDesc(language);
                if (locDesc != null) {
                    addPhraseSearchField(doc, searchField, locDesc.getDescription());
                }
                location.getPoints().forEach(point -> {
                    PointDesc pointDesc = point.getDesc(language);
                    if (pointDesc != null) {
                        addPhraseSearchField(doc, searchField, pointDesc.getDescription());
                    }
                });
            });
        }

        // Add the spatial data to the index
        message.getLocations().forEach(location -> {
            try {
                addShapeSearchFields(doc, location.toWkt());
            } catch (Exception e) {
                log.warn("Not indexing location for message " + message.getId() + " because of error " + e);
            }
        });
    }

    /**
     * Produces a chained lucene filter based on the location list
     * @param locations the list of locations to produce a filter for
     * @return the lucene filter or null if no locations are defiend.
     */
    public Filter getLocationFilter(List<Location> locations) throws ParseException {
        if (locations.size() == 0) {
            return null;
        }
        List<Filter> filters = new ArrayList<>();
        for (Location loc : locations) {
            SpatialArgs args = new SpatialArgs(SpatialOperation.Intersects, loc.toWkt());
            filters.add(strategy.makeFilter(args));
        }
        return new ChainedFilter(filters.toArray(new Filter[filters.size()]), ChainedFilter.OR);
    }

    /**
     * Main search method
     *
     * @param param the search parameters
     * @return the resulting list of messages
     */
    public MessageSearchResult search(MessageSearchParams param) {
        long t0 = System.currentTimeMillis();
        MessageSearchResult result = new MessageSearchResult();
        result.setStartIndex(param.getStartIndex());

        try {
            // For efficiency reasons, two queries are executed:
            //
            // Query 1: Since we need to return the total result count, the first
            // query performs the full query based on the search parameters, but
            // fetches only the id's of the messages.
            //
            // Query 2: Then the messages with the paged set of id's are fetched in full.

            // **********************************************************************************/
            // ********** Query 1: Fetch ids of all messages matching search parameters  ********/
            // **********************************************************************************/
            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<Tuple> tupleQuery = builder.createTupleQuery();

            // Select messages
            Root<Message> msgRoot = tupleQuery.from(Message.class);
            msgRoot.join("seriesIdentifier", JoinType.LEFT);
            javax.persistence.criteria.Path<SeriesIdentifier> msgId = msgRoot.get("seriesIdentifier");

            // Build the predicates based on the search parameters
            PredicateHelper<Tuple> tuplePredicateBuilder = new PredicateHelper<>(builder, tupleQuery)
                    .equals(msgRoot.get("status"), param.getStatus())
                    .between(msgRoot.get("created"), param.getFrom(), param.getTo());

            // Compute the type closure
            Set<Type> types = new HashSet<>();
            types.addAll(param.getTypes());
            param.getMainTypes().forEach(mt -> {
                for (Type t : Type.values()) {
                    if (t.getSeriesIdType() == mt) {
                        types.add(t);
                    }
                }
            });

            if (types.size() > 0) {
                tuplePredicateBuilder.in(msgRoot.get("type"), types);
            }

            // Search the Lucene index for free text search and location information
            if (param.requiresLuceneSearch()) {
                Filter filter = null;
                if (param.getLocations() != null) {
                    filter = getLocationFilter(param.getLocations());
                }
                List<Long> ids = searchIndex(param.getQuery(), searchField(param.getLanguage()), filter, Integer.MAX_VALUE);
                tuplePredicateBuilder.in(msgRoot.get("id"), ids);
            }

            // Filter on areas
            if (param.getAreaIds().size() > 0) {

                // Note to self: A more efficient way would be to join on area and match
                // the lineage of the joined area with that of the message area...
                msgRoot.join("area", JoinType.LEFT);
                javax.persistence.criteria.Path<Area> area = msgRoot.get("area");
                Predicate[] areaMatch = new Predicate[param.getAreaIds().size()];
                Iterator<Integer> i = param.getAreaIds().iterator();
                for (int x = 0; x < areaMatch.length; x++) {
                    String lineage = em.find(Area.class, i.next()).getLineage();
                    areaMatch[x] = builder.like(area.get("lineage"), lineage + "%");
                }
                tuplePredicateBuilder.add(builder.or(areaMatch));

                //msgRoot.join("area", JoinType.LEFT);
                //javax.persistence.criteria.Path<Area> area = msgRoot.get("area");
                //tuplePredicateBuilder.startsWith(area.get("lineage"), area);
            }

            // Filter on charts
            if (param.getChartIds().size() > 0) {

                Join<Message, Chart> charts = msgRoot.join("charts", JoinType.LEFT);
                Predicate[] chartMatch = new Predicate[param.getChartIds().size()];
                Iterator<Integer> i = param.getChartIds().iterator();
                for (int x = 0; x < chartMatch.length; x++) {
                    chartMatch[x] = builder.equal(charts.get("id"), i.next());
                }
                tuplePredicateBuilder.add(builder.or(chartMatch));
            }

            // Complete the query and fetch the message id's (and validFrom, year and number for sorting)
            tupleQuery.multiselect(msgRoot.get("id"), msgRoot.get("validFrom"), msgId.get("year"), msgId.get("number"), msgId.get("mainType"), msgId.get("authority"))
                    .distinct(true)
                    .where(tuplePredicateBuilder.where());
            sortQuery(param, builder, tupleQuery, msgRoot, msgId);

            // Execute the query
            List<Tuple> totalResult = em
                    .createQuery(tupleQuery)
                    .getResultList();

            // Register the total result and pick out the message ids for the paged result
            result.setTotal(totalResult.size());
            List<Integer> msgIds = totalResult
                    .stream()
                    .map(t -> (Integer) t.get(0))
                    .collect(Collectors.toList());

            List<Integer> pagedMsgIds = msgIds.subList(
                    Math.min(param.getStartIndex(), msgIds.size()),
                    Math.min(param.getStartIndex() + param.getMaxHits(), msgIds.size()));

            // **********************************************************************************/
            // ********** Query 2: Fetch messages with the paged set of id's             ********/
            // **********************************************************************************/

            // Check if the message number exceeds the maximum allowed message number
            if (param.isMapMode() && pagedMsgIds.size() > maxMessageNo.intValue()) {

                // Will typically only ever happen en Map view mode.
                // By flagging overflow, the client can e.g. show bitmap layer instead
                result.setOverflowed(true);

            } else {

                // Fetch the cached messages
                List<Message> messages = messageService.getCachedMessages(pagedMsgIds);

                DataFilter filter;
                if (param.isMapMode()) {
                    filter = DataFilter.get("Message.locations", "MessageDesc.title").setLang(param.getLanguage());
                } else {
                    filter = DataFilter.get("Message.details", "Area.parent", "Category.parent").setLang(param.getLanguage());
                }
                result.addMessages(messages, filter);
            }

            log.trace("Message search result: " + result + " in " +
                    (System.currentTimeMillis() - t0) + " ms");

            return result;

        } catch (Exception e) {
            log.error("Error performing search " + param + ": " + e, e);
            return result;
        }
    }

    /**
     * Sorts the criteria query according to the parameters
     *
     * @param param the search parameters
     * @param cq the criteria query
     */
    private <M, T, I> void sortQuery(MessageSearchParams param, CriteriaBuilder builder, CriteriaQuery<T> cq, Root<M> root, javax.persistence.criteria.Path<I> msgId) {
        if (MessageSearchParams.SortBy.DATE == param.getSortBy()) {
            if (param.getSortOrder() == MessageSearchParams.SortOrder.ASC) {
                cq.orderBy(builder.asc(root.get("validFrom")));
            } else {
                cq.orderBy(builder.desc(root.get("validFrom")));
            }
        } else if (MessageSearchParams.SortBy.ID == param.getSortBy()) {
            if (param.getSortOrder() == MessageSearchParams.SortOrder.ASC) {
                // cq.orderBy(builder.asc(msgId.get("mainType")), builder.asc(msgId.get("authority")), builder.asc(msgId.get("year")), builder.asc(msgId.get("number")));
                cq.orderBy(builder.asc(msgId.get("year")), builder.asc(msgId.get("number")));
            } else {
                // cq.orderBy(builder.desc(msgId.get("mainType")), builder.desc(msgId.get("authority")), builder.desc(msgId.get("year")), builder.desc(msgId.get("number")));
                cq.orderBy(builder.desc(msgId.get("year")), builder.desc(msgId.get("number")));
            }
        }
    }

}
