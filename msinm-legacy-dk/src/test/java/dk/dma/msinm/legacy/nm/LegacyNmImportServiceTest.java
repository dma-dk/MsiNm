package dk.dma.msinm.legacy.nm;

import dk.dma.msinm.common.MsiNmApp;
import dk.dma.msinm.common.audit.AuditEntry;
import dk.dma.msinm.common.audit.AuditorFactory;
import dk.dma.msinm.common.config.LogConfiguration;
import dk.dma.msinm.common.db.SqlProducer;
import dk.dma.msinm.common.sequence.SequenceEntity;
import dk.dma.msinm.common.sequence.Sequences;
import dk.dma.msinm.common.settings.Settings;
import dk.dma.msinm.common.settings.SettingsEntity;
import dk.dma.msinm.legacy.msi.model.LegacyMessage;
import dk.dma.msinm.legacy.msi.service.LegacyMessageService;
import dk.dma.msinm.model.Area;
import dk.dma.msinm.model.AreaDesc;
import dk.dma.msinm.model.Category;
import dk.dma.msinm.model.CategoryDesc;
import dk.dma.msinm.model.Chart;
import dk.dma.msinm.model.Location;
import dk.dma.msinm.model.LocationDesc;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.model.MessageDesc;
import dk.dma.msinm.model.SeriesIdentifier;
import dk.dma.msinm.model.Point;
import dk.dma.msinm.model.PointDesc;
import dk.dma.msinm.test.MsiNmUnitTest;
import dk.dma.msinm.test.TestDatabaseConfiguration;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the {@linkplain NmPdfExtractor} class
 */
@RunWith(CdiRunner.class)
@AdditionalClasses(value = {
        TestDatabaseConfiguration.class, SqlProducer.class, LegacyNmImportService.class, LegacyMessageService.class, Settings.class,
        Sequences.class, LogConfiguration.class, AuditorFactory.class, EntityManager.class, MsiNmApp.class
})
public class LegacyNmImportServiceTest extends MsiNmUnitTest
{

    @Inject
    Logger log;

    @Inject
    LegacyNmImportService nmImportService;

    @BeforeClass
    public static void prepareEntityManagerFactory() throws ClassNotFoundException {
        prepareEntityManagerFactory(
                SequenceEntity.class, SettingsEntity.class, AuditEntry.class,
                LegacyMessage.class,
                Message.class, MessageDesc.class, Location.class, LocationDesc.class,
                Area.class, AreaDesc.class, Category.class, CategoryDesc.class,
                Chart.class, Point.class, PointDesc.class, SeriesIdentifier.class
        );
    }


    @Test
    public void test() throws Exception {
        File pdf = Paths.get(getClass().getResource("/2014 EfS 21.pdf").toURI()).toFile();
        assertNotNull(pdf);

        List<Message> notices = nmImportService.importPDF(pdf);
        assertEquals(38, notices.size());

    }

}