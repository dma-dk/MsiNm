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
import dk.dma.msinm.common.model.VersionedEntity;
import org.apache.lucene.document.Document;

import javax.ejb.Singleton;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Test index used in unit tests
 */
@Singleton
public class TestLuceneIndex extends AbstractLuceneIndex<TestLuceneIndex.TestEntity> {

    final static String SEARCH_FIELD = "q";
    static int counter = 0;

    List<TestEntity> data = new ArrayList<>();
    Path folder = Files.createTempDir().toPath();

    public TestLuceneIndex addData(String text) {
        TestEntity e = new TestEntity(text);
        e.setCreated(new Date());
        e.setUpdated(e.getCreated());
        data.add(e);
        return this;
    }

    @Override
    protected Path getIndexFolder() {
        return folder;
    }

    @Override
    protected List<TestEntity> findUpdatedEntities(Date fromDate, int maxCount) {
        return data.stream()
                .filter(e -> e.getUpdated()
                .after(fromDate))
                .collect(Collectors.toList());
    }

    @Override
    protected void addEntityToDocument(Document doc, TestEntity entity) {
        addPhraseSearchField(doc, SEARCH_FIELD, entity.toString());
    }

    public static class TestEntity extends VersionedEntity<Integer> {
        String text;

        public TestEntity(String text) {
            id = counter++;
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
