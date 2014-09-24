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
package dk.dma.msinm.service;

import dk.dma.msinm.common.config.LogConfiguration;
import dk.dma.msinm.common.db.SqlProducer;
import dk.dma.msinm.common.settings.Settings;
import dk.dma.msinm.common.settings.SettingsEntity;
import dk.dma.msinm.common.templates.TemplateConfiguration;
import dk.dma.msinm.model.Area;
import dk.dma.msinm.model.AreaDesc;
import dk.dma.msinm.model.Location;
import dk.dma.msinm.model.LocationDesc;
import dk.dma.msinm.model.Point;
import dk.dma.msinm.model.PointDesc;
import dk.dma.msinm.test.MsiNmUnitTest;
import dk.dma.msinm.test.TestResources;
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
        TestResources.class, TemplateConfiguration.class, SqlProducer.class, Settings.class,
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
