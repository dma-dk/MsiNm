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

import com.google.common.io.Files;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Check that we can use the Lucene commit user data for ... metadata
 */
public class CommitUserDataTest {


    @Test
    public void test() throws IOException {

        File indexFolder = Files.createTempDir();
        Directory directory= FSDirectory.open(indexFolder);

        // Create an index writer
        IndexWriterConfig iwc = new IndexWriterConfig(LuceneUtils.LUCENE_VERSION, new StandardAnalyzer(LuceneUtils.LUCENE_VERSION));
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        IndexWriter indexWriter = new IndexWriter(directory, iwc);

        // Write a document
        Document doc = new Document();
        doc.add(new IntField("id", 100, Field.Store.YES));
        indexWriter.addDocument(doc);

        // Add user data
        Map<String, String> userData = new HashMap<>();
        userData.put("A", "B");
        indexWriter.setCommitData(userData);
        indexWriter.close();

        // Check if we can read user data
        DirectoryReader indexReader = DirectoryReader.open(FSDirectory.open(indexFolder));
        assertEquals("B", indexReader.getIndexCommit().getUserData().get("A"));

    }
}
