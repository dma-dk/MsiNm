package dk.dma.msinm.common;

import dk.dma.msinm.common.audit.AuditEntry;
import dk.dma.msinm.common.audit.AuditService;
import dk.dma.msinm.common.audit.Auditor;
import dk.dma.msinm.common.audit.AuditorFactory;
import dk.dma.msinm.common.config.LogConfiguration;
import dk.dma.msinm.common.db.SqlProducer;
import dk.dma.msinm.test.MsiNmUnitTest;
import dk.dma.msinm.test.TestDatabaseConfiguration;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * Unit tests for the Sequence
 */
@RunWith(CdiRunner.class)
@AdditionalClasses(value = {
        TestDatabaseConfiguration.class, SqlProducer.class, AuditorFactory.class,
        LogConfiguration.class, EntityManager.class
})
public class AuditorTest extends MsiNmUnitTest {

    @Inject
    Auditor auditor;

    @Inject
    AuditService auditService;

    @BeforeClass
    public static void prepareEntityManagerFactory() throws ClassNotFoundException {
        prepareEntityManagerFactory(
                AuditEntry.class
        );
    }

    @Test
    public void sequenceTest() throws InterruptedException {

        // Log an entry
        auditor.info("This is the %d test", 1);

        // Since logging is asynchronous, wait a bit
        Thread.sleep(1000);

        List<AuditEntry> audits = auditService.getAll(AuditEntry.class);
        Assert.assertEquals(1, audits.size());

        AuditEntry entry = audits.get(0);

        // Check that the values a valid
        Assert.assertEquals(AuditEntry.Level.OK, entry.getLevel());
        Assert.assertEquals("This is the 1 test", entry.getMessage());
        Assert.assertEquals(getClass().getName(), entry.getModule());
    }

}
