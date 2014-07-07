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
