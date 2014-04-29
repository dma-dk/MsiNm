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

import dk.dma.msinm.common.config.DatabaseConfiguration;
import dk.dma.msinm.common.config.LogConfiguration;
import dk.dma.msinm.common.sequence.Sequences;
import dk.dma.msinm.common.settings.Settings;
import org.apache.lucene.queryparser.classic.ParseException;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Test the Lucene index
 */
@RunWith(CdiRunner.class)
@AdditionalClasses(value = {
        DatabaseConfiguration.class, Settings.class,
        Sequences.class, LogConfiguration.class, EntityManager.class
})
public class LuceneIndexTest {

    @Inject
    TestLuceneIndex testLuceneIndex;

    @Test
    public void test() throws IOException, ParseException {
        // Add three documents and see that they are indexed
        testLuceneIndex.addData("hello world").addData("the world is flat").addData("pancakes are flat");
        assertEquals(3, testLuceneIndex.updateLuceneIndex(100, true));

        // Check that they do not get re-indexed
        assertEquals(0, testLuceneIndex.updateLuceneIndex(100, true));

        // Add a documents and see that it is indexed
        testLuceneIndex.addData("Ergo, the world is a pancake");
        assertEquals(1, testLuceneIndex.updateLuceneIndex(100, true));

        // Test a few searches
        assertEquals(3, testLuceneIndex.searchIndex("world", TestLuceneIndex.SEARCH_FIELD, null, 100).size());
        assertEquals(3, testLuceneIndex.searchIndex("WORLD", TestLuceneIndex.SEARCH_FIELD, null, 100).size());
        assertEquals(1, testLuceneIndex.searchIndex("'hello world'", TestLuceneIndex.SEARCH_FIELD, null, 100).size());
        assertEquals(1, testLuceneIndex.searchIndex("hello world", TestLuceneIndex.SEARCH_FIELD, null, 100).size());
        assertEquals(1, testLuceneIndex.searchIndex("world and pancake", TestLuceneIndex.SEARCH_FIELD, null, 100).size());
        assertEquals(4, testLuceneIndex.searchIndex("world or pancake?", TestLuceneIndex.SEARCH_FIELD, null, 100).size());
        assertEquals(2, testLuceneIndex.searchIndex("pan*", TestLuceneIndex.SEARCH_FIELD, null, 100).size());
        assertEquals(1, testLuceneIndex.searchIndex("pan* and not world", TestLuceneIndex.SEARCH_FIELD, null, 100).size());
    }
}
