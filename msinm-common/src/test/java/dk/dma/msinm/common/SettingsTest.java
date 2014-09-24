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

import dk.dma.msinm.common.config.LogConfiguration;
import dk.dma.msinm.common.db.SqlProducer;
import dk.dma.msinm.common.settings.Settings;
import dk.dma.msinm.common.settings.SettingsEntity;
import dk.dma.msinm.test.MsiNmUnitTest;
import dk.dma.msinm.test.TestResources;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;

/**
 * Unit tests for the Settings
 */
@RunWith(CdiRunner.class)
@AdditionalClasses(value = {
        TestResources.class, SqlProducer.class, Settings.class,
        LogConfiguration.class, EntityManager.class
})
public class SettingsTest extends MsiNmUnitTest {

    @Inject
    @dk.dma.msinm.common.settings.annotation.Setting(value = "testSettting1", defaultValue = "hello mum!")
    String testSetting1;

    @Inject
    @dk.dma.msinm.common.settings.annotation.Setting(value = "testSettting2", defaultValue = "hello dad!")
    String testSetting2;

    @Inject
    @dk.dma.msinm.common.settings.annotation.Setting(value = "testSettting3", defaultValue = "User home: ${user.home}", substituteSystemProperties = true)
    String testSetting3;

    @Inject
    @dk.dma.msinm.common.settings.annotation.Setting(value = "testSettting4", defaultValue = "999")
    Long testSetting4;

    @BeforeClass
    public static void prepareEntityManagerFactory() throws ClassNotFoundException {
        prepareEntityManagerFactory(
                SettingsEntity.class
        );
    }

    @Test
    public void settingsTest() {

        // Test setting defined by its default property
        Assert.assertEquals("hello mum!", testSetting1);

        // Test setting defined by the settings.properties file
        Assert.assertEquals("NOT hello dad!", testSetting2);

        // Test substitution of system properties
        Assert.assertEquals("User home: " + System.getProperty("user.home"), testSetting3);

        // Test other types (Long)
        Assert.assertEquals(testSetting4, new Long(999));
    }


}
