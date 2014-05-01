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
package dk.dma.msinm.lucene;

import dk.dma.msinm.common.model.VersionedEntity;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Future;

/**
 * Base class for Lucene index beans
 */
public abstract class AbstractLuceneIndex<T extends VersionedEntity<?>> {

    protected final static String ID_FIELD				= "id";
    protected final static String LAST_UPDATE   		= "lastUpdate";
    protected final static int MAX_INDEX_COUNT 			= 100;
    protected final static int OPTIMIZE_INDEX_COUNT 	= 100;
    protected final static int MAX_NUM_SEGMENTS	 		= 4;

    @Inject
    Logger log;

    DirectoryReader reader;
    int optimizeIndexCount = 0;
    boolean locked = false;

    /**
     * Returns the folder used for the index
     * @return the folder used for the index
     */
    protected abstract Path getIndexFolder();

    /**
     * Returns the list of entities updated since the given date
     * @param fromDate the date after which to look for changed entities
     * @param maxCount the max number of entities to return
     * @return the updated entities
     */
    protected abstract List<T> findUpdatedEntities(Date fromDate, int maxCount);

    /**
     * Adds the given entity to the given document
     *
     * @param doc the document to add the entity to
     * @param entity the entity to add
     */
    protected abstract void addEntityToDocument(Document doc, T entity);

    /**
     * Creates and returns a Lucene writer
     */
    public IndexWriter getNewWriter() throws IOException {

        StandardAnalyzer analyzer = new StandardAnalyzer(LuceneUtils.LUCENE_VERSION);
        IndexWriterConfig iwc = new IndexWriterConfig(LuceneUtils.LUCENE_VERSION, analyzer);
        // Add new documents to an existing index:
        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);

        Path indexFolder = getIndexFolder();
        try {
            Directory dir = FSDirectory.open(indexFolder.toFile());
            return new IndexWriter(dir, iwc);
        } catch (IOException ex) {
            log.error("Failed to create Customer Lucene Index in folder " + indexFolder, ex);
            throw ex;
        }
    }

    /**
     * Returns the cached index reader, or creates one if none is defined
     * @return the shared index reader
     */
    public DirectoryReader getIndexReader() throws IOException {
        if (reader == null) {
            Path indexFolder = getIndexFolder();
            try {
                reader = DirectoryReader.open(FSDirectory.open(indexFolder.toFile()));
            } catch (IOException ex) {
                log.error("Failed to open Lucene Index in folder " + indexFolder);
                throw ex;
            }
        }
        return reader;
    }

    /**
     * Closes the given writer
     * @param writer the writer to close
     */
    public void closeWriter(IndexWriter writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                log.warn("Error closing writer");
            }
        }
    }

    /**
     * Closes the current reader
     */
    public void closeReader() {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                log.warn("Error closing reader");
            }
            reader = null;
        }
    }

    /**
     * Refreshes the current reader from the given writer
     *
     * @param writer the index writer
     */
    protected void refreshReader(IndexWriter writer) throws IOException {
        closeReader();
        reader = DirectoryReader.open(writer, true);
    }

    /**
     * Call this to re-index the entity index completely
     * @throws IOException
     */
    @Asynchronous
    public Future<Integer> recreateIndexAsync() throws IOException {
        int updateCount = recreateIndex();
        return new AsyncResult<>(updateCount);
    }

    /**
     * Call this to re-index the entity index completely
     * @throws IOException
     */
    public int recreateIndex() throws IOException {
        // Check if we are already in the middle of re-indexing
        if (locked) {
            return 0;
        }

        // Go ahead and re-index all
        locked = true;
        try {
            // delete the old index
            deleteIndex();

            // Update all customers
            return updateLuceneIndex(Integer.MAX_VALUE, true);

        } finally {
            locked = false;
        }
    }

    /**
     * Deletes the current index
     * @throws IOException
     */
    public void deleteIndex() throws IOException {
        // Delete the index
        IndexWriter writer = null;
        try {
            writer = getNewWriter();
            writer.deleteAll();
        } finally {
            closeWriter(writer);
        }
    }

    /**
     * Returns the last updated time
     * @return the last updated time
     */
    private Date getLastUpdated() {
        try {
            DirectoryReader reader = getIndexReader();
            if (reader.getIndexCommit().getUserData().containsKey(LAST_UPDATE)) {
                return new Date(Long.valueOf(reader.getIndexCommit().getUserData().get(LAST_UPDATE)));
            }
        } catch (Exception e) {
            log.debug("Could not get last-updated flag from index reader");
        }
        return new Date(0);
    }

    /**
     * Sets the last updated time
     * @param date the last updated time
     */
    private void setLastUpdated(Date date, IndexWriter writer) {
        Map<String,String> userData = new HashMap<>();
        userData.put(LAST_UPDATE, String.valueOf(date.getTime()));
        writer.setCommitData(userData);
    }

    /**
     * Updates the Lucene index
     *
     * @param maxIndexCount max number of entities to index at a time
     * @param force update even if the locked flag is set
     * @return the number of updates
     */
    public int updateLuceneIndex(int maxIndexCount, boolean force) {
        // Check if we are in the middle of re-indexing
        if (!force && locked) {
            return 0;
        }

        Date lastUpdated = getLastUpdated();

        long t0 = System.currentTimeMillis();
        log.info(String.format("Indexing at most %d changed entities since %s", maxIndexCount, lastUpdated));

        IndexWriter writer = null;
        try {
            // Find all customers changed since the lastUpdated time stamp
            List<T> updatedEntities = findUpdatedEntities(lastUpdated, maxIndexCount);
            if (updatedEntities.size() == 0) {
                return 0;
            }


            // Create a new index writer
            writer = getNewWriter();

            // Update the index with the changes
            for (T entity : updatedEntities) {
                indexEntity(writer, entity);
                if (entity.getUpdated().after(lastUpdated)) {
                    lastUpdated = entity.getUpdated();
                }
            }

            // Update the last-updated flag
            setLastUpdated(lastUpdated, writer);

            // Commit the changes
            writer.commit();

            // Re-open the reader from the writer
            refreshReader(writer);

            // Check if we need to optimize the index
            optimizeIndexCount += updatedEntities.size();
            if (optimizeIndexCount > OPTIMIZE_INDEX_COUNT) {
                writer.forceMerge(MAX_NUM_SEGMENTS);
                optimizeIndexCount = 0;
            }

            log.info("Indexed " + updatedEntities.size() + " entities in "
                    + (System.currentTimeMillis() - t0) + " ms");

            return updatedEntities.size();
        } catch (Exception ex) {
            log.error("Error updating Lucene index: " + ex.getMessage(), ex);
            return 0;
        } finally {
            closeWriter(writer);
        }
    }

    /**
     * Indexes the given entity by deleting and adding the document
     *
     * @param entity the entity to index
     */
    protected void indexEntity(IndexWriter writer, T entity) {
        // First delete the entity
        deleteEntityFromIndex(writer, entity);
        // Then add the entity
        if (shouldAddEntity(entity)) {
            addEntityToIndex(writer, entity);
        }
    }

    /**
     * By default, add all eligible entities.
     * Sub-class may want to e.g. check a status flag
     * @param entity the entity to check
     * @return whether to add the entity to the index
     */
    protected boolean shouldAddEntity(T entity) {
        return true;
    }

    /**
     * Deletes the given entity from the index
     *
     * @param entity the entity to delete
     */
    protected void deleteEntityFromIndex(IndexWriter writer, T entity) {
        try {
            Term idTerm = new Term(ID_FIELD, entity.getId().toString());
            writer.deleteDocuments(idTerm);
        } catch (IOException e) {
            log.debug("Error deleting entity " + entity.getId());
        }
    }



    /**
     * Adds the given entity to the index
     *
     * @param entity the entity to add
     */
    protected void addEntityToIndex(IndexWriter writer, T entity) {
        Document doc = new Document();

        // ID field
        doc.add(new StringField(ID_FIELD, entity.getId().toString(), Field.Store.YES));

        // Add the entity specific fields
        addEntityToDocument(doc, entity);

        // Add the document to the index
        try {
            writer.addDocument(doc);
        } catch (IOException ex) {
            log.error("Error adding entity " + entity.getId() + " to the Lucene index: " + ex.getMessage(), ex);
        }
    }

    /**
     * If the given value is not null, it is added to the search index
     *
     * @param doc the document to add the field value to
     * @param obj the value to add
     */
    protected void addPhraseSearchField(Document doc, String field, Object obj) {
        if (obj != null) {
            String str = (obj instanceof String) ? (String)obj : obj.toString();
            if (StringUtils.isNotBlank(str)) {
                doc.add(new PhraseSearchLuceneField(field, str));
            }
        }
    }

    /**
     * If the given value is not null, it is added to the search index
     *
     * @param doc the document to add the field value to
     * @param obj the value to add
     * @param store the store value of the field
     */
    protected void addStringSearchField(Document doc, String field, Object obj, Field.Store store) {
        if (obj != null) {
            String str = (obj instanceof String) ? (String)obj : obj.toString();
            if (StringUtils.isNotBlank(str)) {
                doc.add(new StringField(field, str, store));
            }
        }
    }

    /**
     * Performs a search in the index and returns the ids of matching entities
     *
     * @param freeTextSearch the search string
     * @param field the field to search
     * @param filter an optional filter
     * @param maxHits the max number of hits to return
     * @return the matching ids
     */
    public List<Long> searchIndex(String freeTextSearch, String field, Filter filter, int maxHits) throws IOException, ParseException {

        Query query;
        if (StringUtils.isNotBlank(freeTextSearch) && StringUtils.isNotBlank(field)) {
            // Normalize query text
            freeTextSearch = LuceneUtils.normalizeQuery(freeTextSearch);

            // Create a query parser with "and" operator as the default
            QueryParser parser = new ComplexPhraseQueryParser(
                    LuceneUtils.LUCENE_VERSION,
                    field,
                    new StandardAnalyzer(LuceneUtils.LUCENE_VERSION));
            parser.setDefaultOperator(QueryParser.OR_OPERATOR);
            //parser.setAllowLeadingWildcard(true); // NB: Expensive!
            query = parser.parse(freeTextSearch);

        } else {
            query = new MatchAllDocsQuery();
        }

        // Perform the search and collect the ids
        IndexSearcher searcher = new IndexSearcher(getIndexReader());
        TopDocs results = (filter == null)
                ? searcher.search(query, maxHits)
                : searcher.search(query, filter, maxHits);

        List<Long> ids = new ArrayList<>();
        for (ScoreDoc hit : results.scoreDocs) {
            Document d = searcher.doc(hit.doc);
            ids.add(Long.valueOf(d.get(ID_FIELD)));
        }
        return ids;
    }
}

