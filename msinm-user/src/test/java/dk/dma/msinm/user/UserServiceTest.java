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
package dk.dma.msinm.user;


import dk.dma.msinm.common.config.LogConfiguration;
import dk.dma.msinm.common.db.SqlProducer;
import dk.dma.msinm.common.mail.MailService;
import dk.dma.msinm.common.sequence.SequenceEntity;
import dk.dma.msinm.common.sequence.Sequences;
import dk.dma.msinm.common.settings.Settings;
import dk.dma.msinm.common.settings.SettingsEntity;
import dk.dma.msinm.test.MsiNmUnitTest;
import dk.dma.msinm.test.TestDatabaseConfiguration;
import dk.dma.msinm.test.TestMailTemplateConfiguration;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test user service
 */
@RunWith(CdiRunner.class)
@AdditionalClasses(value = {
        UserService.class,
        TestDatabaseConfiguration.class, TestMailTemplateConfiguration.class, SqlProducer.class, Settings.class, MailService.class,
        Sequences.class, LogConfiguration.class, EntityManager.class, LogConfiguration.class
})
public class UserServiceTest extends MsiNmUnitTest {

    @Inject
    UserService userService;

    @BeforeClass
    public static void prepareEntityManagerFactory() throws ClassNotFoundException {
        prepareEntityManagerFactory(
                SequenceEntity.class, SettingsEntity.class, User.class, Role.class
        );
    }

    @Test
    public void testPasswordStrength() {

        // Check various passwords
        assertFalse(userService.validatePasswordStrength(null));
        assertFalse(userService.validatePasswordStrength(""));
        assertFalse(userService.validatePasswordStrength("123456"));
        assertFalse(userService.validatePasswordStrength("aaaaaa"));
        assertFalse(userService.validatePasswordStrength("******"));
        assertFalse(userService.validatePasswordStrength("aaaaaaaaaaaaaaaaaaaa1"));

        assertTrue(userService.validatePasswordStrength("12345a"));
        assertTrue(userService.validatePasswordStrength("1aaaaa"));
        assertTrue(userService.validatePasswordStrength("****1a"));

    }
}