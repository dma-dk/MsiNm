package dk.dma.msinm.service;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Shape;
import dk.dma.msinm.common.MsiNmApp;
import dk.dma.msinm.common.db.PredicateHelper;
import dk.dma.msinm.common.settings.annotation.Setting;
import dk.dma.msinm.lucene.AbstractLuceneIndex;
import dk.dma.msinm.model.Area;
import dk.dma.msinm.model.AreaDesc;
import dk.dma.msinm.model.Category;
import dk.dma.msinm.model.CategoryDesc;
import dk.dma.msinm.model.LocationDesc;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.model.PointDesc;
import dk.dma.msinm.vo.CopyOp;
import dk.dma.msinm.vo.LocationVo;
import dk.dma.msinm.vo.MessageVo;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexableField;
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
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    @Setting(value = "messageIndexDir", defaultValue = "${user.home}/.msinm/msg-index", substituteSystemProperties = true)
    Path indexFolder;

    @Inject
    @Setting(value = "messageIndexSpatialLevels", defaultValue = "11")
    Long maxSpatialLevels;  // a value of 11 results in sub-meter precision for geohash

    @Inject
    @Setting(value = "messageIndexDeleteOnStartup", defaultValue = "true")
    Boolean deleteOnStartup;

    SpatialStrategy strategy;

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
        return messageService.findUpdatedMessages(fromDate, maxCount);
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

            addStringSearchField(doc, searchField, message.getStatus(), Field.Store.NO);
            addPhraseSearchField(doc, searchField, message.getSeriesIdentifier().getAuthority());
            addPhraseSearchField(doc, searchField, String.valueOf(message.getSeriesIdentifier().getYear()));

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
                addStringSearchField(doc, searchField, chart.getChartNumber(), Field.Store.NO);
                addStringSearchField(doc, searchField, chart.getInternationalNumber(), Field.Store.NO);
            });

            // Horizontal datum
            addStringSearchField(doc, searchField, message.getHorizontalDatum(), Field.Store.NO);

            // Add language specific fields
            message.getDescs().forEach(desc -> {
                addPhraseSearchField(doc, searchField, desc.getTitle());
                addPhraseSearchField(doc, searchField, desc.getDescription());
                addPhraseSearchField(doc, searchField, desc.getOtherCategories());
                addPhraseSearchField(doc, searchField, desc.getVicinity());
            });

            message.getLightsListNumbers()
                    .forEach(lightsListNumber -> addStringSearchField(doc, searchField, lightsListNumber, Field.Store.NO));

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

            // Build the predicates based on the search parameters
            PredicateHelper<Tuple> tuplePredicateBuilder = new PredicateHelper<>(builder, tupleQuery)
                    .equals(msgRoot.get("status"), param.getStatus())
                    .between(msgRoot.get("created"), param.getFrom(), param.getTo());

            if (param.getTypes().size() > 0) {
                tuplePredicateBuilder.in(msgRoot.get("type"), param.getTypes());
            }

            // Search the Lucene index for free text search and location information
            if (param.requiresLuceneSearch()) {
                Filter filter = null;
                if (param.getLocation() != null) {
                    SpatialArgs args = new SpatialArgs(SpatialOperation.Intersects, param.getLocation().toWkt());
                    filter = strategy.makeFilter(args);
                }
                List<Long> ids = searchIndex(param.getQuery(), searchField(param.getLanguage()), filter, Integer.MAX_VALUE);
                tuplePredicateBuilder.in(msgRoot.get("id"), ids);
            }

            // Complete the query and fetch the message id's (and validFrom for sorting)
            tupleQuery.multiselect(msgRoot.get("id"), msgRoot.get("validFrom"))
                    .distinct(true)
                    .where(tuplePredicateBuilder.where());
            sortQuery(param, builder, tupleQuery, msgRoot);

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
            CriteriaQuery<Message> msgQuery = builder.createQuery(Message.class);
            msgRoot = msgQuery.from(Message.class);

            // Build the predicate
            PredicateHelper<Message> msgPredicateBuilder = new PredicateHelper<>(builder, msgQuery)
                    .in(msgRoot.get("id"), pagedMsgIds);

            // Complete the query
            msgQuery.select(msgRoot)
                    .distinct(true)
                    .where(msgPredicateBuilder.where());
            sortQuery(param, builder, msgQuery, msgRoot);

            // Execute the query and update the search result
            List<Message> pagedResult = em
                    .createQuery(msgQuery)
                    .getResultList();

            // Copy the specified language and parent references of the included Area
            result.addMessages(pagedResult, CopyOp.get(CopyOp.PARENT).setLang(param.getLanguage()));

            log.trace("Message search result: " + result + " in " +
                    (System.currentTimeMillis() - t0) + " ms");

            return result;

        } catch (Exception e) {
            log.error("Error performing search " + param + ": " + e, e);
            return result;
        }
    }

    /**
     * TODO
     */
    public List<LocationVo> searchLocations(MessageSearchParams param) {
        List<LocationVo> result = new ArrayList<>();

        try {
            MessageSearchResult messages = search(param);

            for (MessageVo msg : messages.getMessages()) {
               msg.getLocations().stream()
                       .filter(loc -> loc.getPoints().size() > 0)
                       .forEach(result::add);
            }
            return result;

        } catch (Exception e) {
            log.error("Error performing search : " + e, e);
            return result;
        }
    }


    /**
     * Sorts the criteria query according to the parameters
     *
     * @param param the search parameters
     * @param cq the criteria query
     */
    private <M, T> void sortQuery(MessageSearchParams param, CriteriaBuilder builder, CriteriaQuery<T> cq, Root<M> root) {
        if (MessageSearchParams.SortBy.DATE == param.getSortBy()) {
            if (param.getSortOrder() == MessageSearchParams.SortOrder.ASC) {
                cq.orderBy(builder.asc(root.get("validFrom")));
            } else {
                cq.orderBy(builder.desc(root.get("validFrom")));
            }
        }
    }
}
