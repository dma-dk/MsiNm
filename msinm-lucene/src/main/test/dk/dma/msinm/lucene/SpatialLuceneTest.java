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

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.shape.Shape;
import dk.dma.msinm.common.config.LogConfiguration;
import junit.framework.TestCase;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.apache.lucene.spatial.query.SpatialArgs;
import org.apache.lucene.spatial.query.SpatialOperation;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.text.ParseException;

import static org.junit.Assert.assertArrayEquals;

/**
 * Largely based on extracts from a SpatialExample Lucene test case:
 * https://github.com/apache/lucene-solr/blob/branch_4x/lucene/spatial/src/test/org/apache/lucene/spatial/SpatialExample.java
 */
@RunWith(CdiRunner.class)
@AdditionalClasses(value = { LogConfiguration.class })
public class SpatialLuceneTest extends TestCase {

    @Inject
    Logger log;

    private SpatialStrategy strategy;
    private SpatialContext ctx = SpatialContext.GEO;
    private Directory directory;

    @Test
    public void test() throws IOException, ParseException {

        int maxLevels = 11;//results in sub-meter precision for geohash
        SpatialPrefixTree grid = new GeohashPrefixTree(ctx, maxLevels);

        strategy = new RecursivePrefixTreeStrategy(grid, "myGeoField");
        directory = new RAMDirectory();


        IndexWriterConfig iwConfig = new IndexWriterConfig(Version.LUCENE_47,null);
        IndexWriter indexWriter = new IndexWriter(directory, iwConfig);
        indexWriter.addDocument(newSampleDocument(2, ctx.makePoint(-80.93, 33.77)));
        indexWriter.addDocument(newSampleDocument(4, ctx.readShapeFromWkt("POINT(60.9289094 -50.7693246)")));
        indexWriter.addDocument(newSampleDocument(20, ctx.makePoint(0.1,0.1), ctx.makePoint(0, 0)));
        indexWriter.addDocument(newSampleDocument(30, JtsSpatialContext.GEO.readShapeFromWkt("POLYGON((0 0, -90 0, -90 40, 0 40, 0 0))")));
        indexWriter.close();

        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        Sort idSort = new Sort(new SortField("id", SortField.Type.INT));

        // Search 1
        SpatialArgs args = new SpatialArgs(SpatialOperation.Intersects,
                ctx.makeCircle(-80.0, 33.0, DistanceUtils.dist2Degrees(200, DistanceUtils.EARTH_MEAN_RADIUS_KM)));
        TopDocs docs = indexSearcher.search(new MatchAllDocsQuery(), strategy.makeFilter(args), 10, idSort);
        assertDocMatchedIds(indexSearcher, docs, 2, 30);

    }


    private Document newSampleDocument(int id, Shape... shapes) {
        Document doc = new Document();
        doc.add(new IntField("id", id, Field.Store.YES));
        for (Shape shape : shapes) {
            for (IndexableField f : strategy.createIndexableFields(shape)) {
                doc.add(f);
            }

            doc.add(new StoredField(strategy.getFieldName(), shape.toString()));
        }
        return doc;
    }

    private void assertDocMatchedIds(IndexSearcher indexSearcher, TopDocs docs, int... ids) throws IOException {
        int[] gotIds = new int[docs.totalHits];
        for (int i = 0; i < gotIds.length; i++) {
            gotIds[i] = indexSearcher.doc(docs.scoreDocs[i].doc).getField("id").numericValue().intValue();
        }
        assertArrayEquals(ids,gotIds);
    }
}
