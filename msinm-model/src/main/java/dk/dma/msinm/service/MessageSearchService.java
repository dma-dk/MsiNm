package dk.dma.msinm.service;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Shape;
import dk.dma.msinm.common.db.MsiNm;
import dk.dma.msinm.common.db.PredicateHelper;
import dk.dma.msinm.common.settings.annotation.Setting;
import dk.dma.msinm.lucene.AbstractLuceneIndex;
import dk.dma.msinm.model.*;
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
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Lucene search index for {@code Message} entities
 */
@Singleton
@Startup
public class MessageSearchService extends AbstractLuceneIndex<Message> {

    final static String SEARCH_FIELD    = "message";
    final static String LOCATION_FIELD  = "location";
    final static String STATUS_FIELD    = "status";

    @Inject
    @MsiNm
    EntityManager em;

    @Inject
    Logger log;

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
        //return entity.getStatus() != MessageStatus.DELETED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addEntityToDocument(Document doc, Message message) {
        addStringSearchField(doc, STATUS_FIELD, message.getStatus(), Field.Store.NO);
        addPhraseSearchField(doc, SEARCH_FIELD, message.getGeneralArea());
        addPhraseSearchField(doc, SEARCH_FIELD, message.getLocality());
        addPhraseSearchField(doc, SEARCH_FIELD, message.getSeriesIdentifier().getAuthority());
        addPhraseSearchField(doc, SEARCH_FIELD, String.valueOf(message.getSeriesIdentifier().getYear()));
        // TODO: Combined series identifier
        // TODO: Type in separate search field?
        for (String specificLocation : message.getSpecificLocations()) {
            addPhraseSearchField(doc, SEARCH_FIELD, specificLocation);
        }
        for (String chartNumber : message.getChartNumbers()) {
            addStringSearchField(doc, SEARCH_FIELD, chartNumber, Field.Store.NO);
        }
        for (Integer intChartNumber : message.getIntChartNumbers()) {
            addStringSearchField(doc, SEARCH_FIELD, String.valueOf(intChartNumber), Field.Store.NO);
        }

        // Add the MSI/NtM specific fields
        if (message instanceof NavwarnMessage) {
            addNavwarnMessageToDocument(doc, (NavwarnMessage)message);
        } else {
            addNoticeMessageToDocument(doc, (NoticeMessage) message);
        }
     }

    /**
     * Adds the MSI message to the index
     * @param doc the Lucene document
     * @param message the MSI message
     */
    protected void addNavwarnMessageToDocument(Document doc, NavwarnMessage message) {
        for (MessageItem messageItem : message.getMessageItems()) {
            addPhraseSearchField(doc, SEARCH_FIELD, messageItem.getKeySubject());
            addPhraseSearchField(doc, SEARCH_FIELD, messageItem.getAmplifyingRemarks());
            // TODO: Priority
            for (MessageLocation location : messageItem.getLocations()) {
                try {
                    addShapeSearchFields(doc, location.toWkt());
                } catch (Exception e) {
                    log.warn("Not indexing location for message " + message.getId() + " because of error " + e);
                }
            }
            // TODO: Category in separate search field?
        }
    }

    /**
     * Adds the NtM message to the index
     * @param doc the Lucene document
     * @param message the NtM message
     */
    protected void addNoticeMessageToDocument(Document doc, NoticeMessage message) {
        addPhraseSearchField(doc, SEARCH_FIELD, message.getAuthority());
        addPhraseSearchField(doc, SEARCH_FIELD, message.getAmplifyingRemarks());
        // TODO: Category in separate search field?
        for (String lightsListNumber : message.getLightsListNumbers()) {
            addStringSearchField(doc, SEARCH_FIELD, lightsListNumber, Field.Store.NO);
        }

        // Add permanent items
        for (PermanentItem permanentItem : message.getPermanentItems()) {
            addPhraseSearchField(doc, SEARCH_FIELD, permanentItem.getAmplifyingRemarks());
            addStringSearchField(doc, SEARCH_FIELD, permanentItem.getChartNumber(), Field.Store.NO);
            addStringSearchField(doc, SEARCH_FIELD, permanentItem.getHorizontalDatum(), Field.Store.NO);
            for (NoticeElement noticeElement : permanentItem.getNoticeElements()) {
                // TODO: Add notice verb in separate field?
                addPhraseSearchField(doc, SEARCH_FIELD, noticeElement.getFeatureOrCharacteristic());
                addPhraseSearchField(doc, SEARCH_FIELD, noticeElement.getAmplifyingNote());
                for (String graphicalRepresentation : noticeElement.getGraphicalRepresentations()) {
                    addPhraseSearchField(doc, SEARCH_FIELD, graphicalRepresentation);
                }
                try {
                    addShapeSearchFields(doc, noticeElement.getLocation().toWkt());
                } catch (Exception e) {
                    log.warn("Not indexing location for message " + message.getId() + " because of error " + e);
                }
            }
        }

        // Add P & T items
        for (TempPreliminaryItem tempPreliminaryItem : message.getTempPreliminaryItems()) {
            addPhraseSearchField(doc, SEARCH_FIELD, tempPreliminaryItem.getItemDescription());
            for (String graphicalRepresentation : tempPreliminaryItem.getGraphicalRepresentations()) {
                addPhraseSearchField(doc, SEARCH_FIELD, graphicalRepresentation);
            }
            try {
                addShapeSearchFields(doc, tempPreliminaryItem.getLocation().toWkt());
            } catch (ParseException e) {
                log.warn("Not indexing location for message " + message.getId() + " because of error " + e);
            }
        }
    }

    /**
     * Main search method
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
            javax.persistence.criteria.Path<MessageSeriesIdentifier> msgId = msgRoot.get("seriesIdentifier");

            // Build the predicates based on the search parameters
            PredicateHelper<Tuple> tuplePredicateBuilder = new PredicateHelper<>(builder, tupleQuery)
                    .equals(msgRoot.get("status"), param.getStatus())
                    .between(msgRoot.get("created"), param.getFrom(), param.getTo());

            if (param.getTypes().size() > 0) {
                tuplePredicateBuilder.in(msgId.get("type"), param.getTypes());
            }

            // Search the Lucene index for free text search and location information
            if (param.requiresLuceneSearch()) {
                Filter filter = null;
                if (param.getLocation() != null) {
                    SpatialArgs args = new SpatialArgs(SpatialOperation.Intersects, param.getLocation().toWkt());
                    filter = strategy.makeFilter(args);
                }
                List<Long> ids = searchIndex(param.getQuery(), SEARCH_FIELD, filter, Integer.MAX_VALUE);
                tuplePredicateBuilder.in(msgRoot.get("id"), ids);
            }

            // Complete the query and fetch the message id's
            tupleQuery.multiselect(msgRoot.get("id"))
                    .distinct(true)
                    .where(tuplePredicateBuilder.where());

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

            // Execute the query and update the search result
            List<Message> pagedResult = em
                    .createQuery(msgQuery)
                    .getResultList();
            result.setMessages(pagedResult);

            log.info("Message search result: " + result + " in " +
                    (System.currentTimeMillis() - t0) + " ms");

            return result;

        } catch (Exception e) {
            log.error("Error performing search " + param + ": " + e, e);
            return result;
        }
    }
}
