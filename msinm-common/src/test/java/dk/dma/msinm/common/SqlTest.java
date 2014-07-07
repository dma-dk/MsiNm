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
package dk.dma.msinm.common;

import dk.dma.msinm.common.db.Sql;
import dk.dma.msinm.common.db.SqlProducer;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

/**
 * Unit tests for the @Sql injection annotation
 */
@RunWith(CdiRunner.class)
@AdditionalClasses(value = {
        SqlProducer.class
})
public class SqlTest {

    @Inject
    @Sql("/sql/sqltest.sql")
    private String testSql;


    @Test
    public void sqlTest() {

        // Test that the sql has been loaded from the sql resource file
        Assert.assertEquals("select * from message", testSql.trim());
    }

}
