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
