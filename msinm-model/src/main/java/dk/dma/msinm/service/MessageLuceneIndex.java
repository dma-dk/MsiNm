package dk.dma.msinm.service;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Shape;
import dk.dma.msinm.common.settings.annotation.Setting;
import dk.dma.msinm.lucene.AbstractLuceneIndex;
import dk.dma.msinm.model.*;
import org.apache.lucene.document.Document;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * Lucene search index for {@code Message} entities
 */
@Singleton
@Startup
public class MessageLuceneIndex extends AbstractLuceneIndex<Message> {

    final static String SEARCH_FIELD = "message";
    final static String LOCATION_FIELD = "location";

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
    public void updateLuceneIndex() {
        updateLuceneIndex(MAX_INDEX_COUNT, false);
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
    protected void addEntityToDocument(Document doc, Message message) {
        addPhraseSearchField(doc, SEARCH_FIELD, message.getGeneralArea());
        addPhraseSearchField(doc, SEARCH_FIELD, message.getLocality());

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
            for (MessageLocation location : messageItem.getLocations()) {
                try {
                    addShapeSearchFields(doc, location.toWkt());
                } catch (ParseException e) {
                    log.warn("Not indexing location for message " + message.getId() + " because of error " + e);
                }
            }
        }
    }

    /**
     * Adds the NtM message to the index
     * @param doc the Lucene document
     * @param message the NtM message
     */
    protected void addNoticeMessageToDocument(Document doc, NoticeMessage message) {

    }

    /**
     * Search the message Lucene index
     *
     * @param freeTextSearch the search string
     * @param location the location to restrict the search to
     * @param maxHits the max number of hits
     * @return the search result
     */
    public List<Long> searchIndex(String freeTextSearch, Shape location, int maxHits) throws Exception {
        Filter filter = null;
        if (location != null) {
            SpatialArgs args = new SpatialArgs(SpatialOperation.Intersects, location);
            filter = strategy.makeFilter(args);
        }
        return super.searchIndex(freeTextSearch, SEARCH_FIELD, filter, maxHits);
    }
}
