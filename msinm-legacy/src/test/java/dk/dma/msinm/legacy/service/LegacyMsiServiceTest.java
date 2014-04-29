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
package dk.dma.msinm.legacy.service;

import dk.dma.msinm.common.config.DatabaseConfiguration;
import dk.dma.msinm.common.config.LogConfiguration;
import dk.dma.msinm.common.sequence.SequenceEntity;
import dk.dma.msinm.common.sequence.Sequences;
import dk.dma.msinm.common.settings.Settings;
import dk.dma.msinm.common.settings.SettingsEntity;
import dk.dma.msinm.legacy.model.LegacyMessage;
import dk.dma.msinm.model.*;
import dk.dma.msinm.test.MsiNmUnitTest;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManager;

/**
 * Tests the {@linkplain dk.dma.msinm.legacy.service.LegacyMsiImportService} class
 */
@RunWith(CdiRunner.class)
@AdditionalClasses(value = {
        DatabaseConfiguration.class, LegacyMsiImportService.class, LegacyMessageService.class, Settings.class,
        Sequences.class, LogConfiguration.class, EntityManager.class
})
public class LegacyMsiServiceTest extends MsiNmUnitTest
{

    @Inject
    Logger log;

    @Inject
    LegacyMsiImportService msiService;

    @BeforeClass
    public static void prepareEntityManagerFactory() throws ClassNotFoundException {
        prepareEntityManagerFactory(
                SequenceEntity.class, SettingsEntity.class,
                LegacyMessage.class,
                Message.class, MessageCategory.class, MessageItem.class, MessageLocation.class,
                MessageSeriesIdentifier.class, NavwarnMessage.class, NoticeElement.class,
                NoticeMessage.class, PermanentItem.class, Point.class, TempPreliminaryItem.class
        );
    }


    @Test
    public void test() {
        log.info(String.format("Fetched %d legacy MSI warnings",
                msiService.importWarnings()));
    }

}
