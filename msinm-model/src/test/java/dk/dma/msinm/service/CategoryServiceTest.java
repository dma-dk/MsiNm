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
import dk.dma.msinm.model.Category;
import dk.dma.msinm.model.CategoryDesc;
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
public class CategoryServiceTest extends MsiNmUnitTest {

    @Inject
    Logger log;

    @Inject
    CategoryService categoryService;

    @BeforeClass
    public static void prepareEntityManagerFactory() throws ClassNotFoundException {
        prepareEntityManagerFactory(
                SettingsEntity.class,
                Category.class, CategoryDesc.class
        );
    }

    @Test
    public void testCategories() throws Exception {

        Category atn = categoryService.createCategory(createCategory("Aids to navigation", "Navigationsværktøjer"), null);

        Category buoy = categoryService.createCategory(createCategory("Buoy", "Bøje"), atn.getId());
        Category light = categoryService.createCategory(createCategory("Light", "Lys"), atn.getId());


        assertEquals(3, categoryService.getAll(Category.class).size());

        assertNotNull(categoryService.findByName("buoy", null, null));
        assertNotNull(categoryService.findByName("buoy", "en", null));
        assertNull(categoryService.findByName("buoy", "da", null));

        assertNotNull(categoryService.findByName("buoy", "en", atn.getId()));
        assertNull(categoryService.findByName("buoy", "en", light.getId()));
    }

    Category createCategory(String nameEnglish, String nameDanish) {
        Category category = new Category();
        category.createDesc("en").setName(nameEnglish);
        category.createDesc("da").setName(nameDanish);
        return category;
    }
}
