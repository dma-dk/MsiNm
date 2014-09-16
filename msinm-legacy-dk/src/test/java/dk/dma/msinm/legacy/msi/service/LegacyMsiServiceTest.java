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
package dk.dma.msinm.legacy.msi.service;

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
import dk.dma.msinm.model.*;
import dk.dma.msinm.service.MessageService;
import dk.dma.msinm.test.MsiNmUnitTest;
import dk.dma.msinm.test.TestDatabaseConfiguration;
import dk.dma.msinm.test.TestTemplateConfiguration;
import dk.dma.msinm.user.UserService;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManager;

/**
 * Tests the {@linkplain LegacyMsiImportRestService} class
 */
@RunWith(CdiRunner.class)
@AdditionalClasses(value = {
        TestDatabaseConfiguration.class, TestTemplateConfiguration.class, SqlProducer.class, LegacyMsiImportRestService.class, LegacyMessageService.class, Settings.class,
        Sequences.class, LogConfiguration.class, AuditorFactory.class, EntityManager.class, MsiNmApp.class
})
public class LegacyMsiServiceTest extends MsiNmUnitTest
{

    @Inject
    Logger log;

    @Inject
    LegacyMsiImportRestService msiService;

    @BeforeClass
    public static void prepareEntityManagerFactory() throws ClassNotFoundException {
        prepareEntityManagerFactory(
                SequenceEntity.class, SettingsEntity.class, AuditEntry.class,
                LegacyMessage.class,
                Message.class, MessageDesc.class, Location.class, LocationDesc.class, Reference.class,
                Area.class, AreaDesc.class, Category.class, CategoryDesc.class,
                Chart.class, Point.class, PointDesc.class, SeriesIdentifier.class, Publication.class
        );
    }


    @Test
    public void test() {
        //log.info(String.format("Fetched %d legacy MSI warnings",
        //        msiService.importWarnings()));
    }

}
