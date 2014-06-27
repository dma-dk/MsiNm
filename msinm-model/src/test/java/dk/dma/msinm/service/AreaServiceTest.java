package dk.dma.msinm.service;

import dk.dma.msinm.common.config.LogConfiguration;
import dk.dma.msinm.common.db.SqlProducer;
import dk.dma.msinm.common.settings.Settings;
import dk.dma.msinm.common.settings.SettingsEntity;
import dk.dma.msinm.model.Area;
import dk.dma.msinm.model.AreaDesc;
import dk.dma.msinm.model.Location;
import dk.dma.msinm.model.LocationDesc;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for the MessageService
 */
@RunWith(CdiRunner.class)
@AdditionalClasses(value = {
        TestDatabaseConfiguration.class, SqlProducer.class, Settings.class,
        LogConfiguration.class, EntityManager.class
})
public class AreaServiceTest extends MsiNmUnitTest {

    @Inject
    Logger log;

    @Inject
    AreaService areaService;

    @BeforeClass
    public static void prepareEntityManagerFactory() throws ClassNotFoundException {
        prepareEntityManagerFactory(
                SettingsEntity.class,
                Area.class, AreaDesc.class, Location.class, LocationDesc.class, Point.class, PointDesc.class
        );
    }

    @Test
    public void testAreas() throws Exception {

        Area world = areaService.createArea(createArea("World", "Jorden"), null);

        Area dk = areaService.createArea(createArea("Denmark", "Danmark"), world.getId());
        Area en = areaService.createArea(createArea("England", "England"), world.getId());


        assertEquals(3, areaService.getAll(Area.class).size());

        assertNotNull(areaService.findByName("denmark", null, null));
        assertNotNull(areaService.findByName("denmark", "en", null));
        assertNull(areaService.findByName("denmark", "da", null));

        assertNotNull(areaService.findByName("denmark", "en", world.getId()));
        assertNull(areaService.findByName("denmark", "en", en.getId()));
    }

    Area createArea(String nameEnglish, String nameDanish) {
        Area area = new Area();
        area.createDesc("en").setName(nameEnglish);
        area.createDesc("da").setName(nameDanish);
        return area;
    }
}
