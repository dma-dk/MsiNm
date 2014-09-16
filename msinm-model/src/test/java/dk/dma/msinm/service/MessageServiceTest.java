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
import dk.dma.msinm.common.sequence.SequenceEntity;
import dk.dma.msinm.common.sequence.Sequences;
import dk.dma.msinm.common.settings.Settings;
import dk.dma.msinm.common.settings.SettingsEntity;
import dk.dma.msinm.model.*;
import dk.dma.msinm.test.MsiNmUnitTest;
import dk.dma.msinm.test.TestDatabaseConfiguration;
import dk.dma.msinm.test.TestTemplateConfiguration;
import dk.dma.msinm.user.UserService;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.io.IOException;
import java.text.ParseException;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for the MessageService
 */
@RunWith(CdiRunner.class)
@AdditionalClasses(value = {
        UserService.class,
        TestDatabaseConfiguration.class, TestTemplateConfiguration.class, SqlProducer.class, Settings.class,
        Sequences.class, LogConfiguration.class, EntityManager.class
})
public class MessageServiceTest extends MsiNmUnitTest {

    @Inject
    Logger log;

    @Inject
    MessageService messageService;

    @Inject
    MessageSearchService messageSearchService;

    @BeforeClass
    public static void prepareEntityManagerFactory() throws ClassNotFoundException {
        prepareEntityManagerFactory(
                SequenceEntity.class, SettingsEntity.class,
                Message.class, MessageDesc.class, Location.class, LocationDesc.class, Reference.class,
                Area.class, AreaDesc.class, Category.class, CategoryDesc.class,
                Chart.class, Point.class, PointDesc.class, SeriesIdentifier.class, Publication.class
        );
    }

    @After
    public void deleteIndex() {
        try {
            messageSearchService.deleteIndex();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testNavwarnMessage() throws Exception {

        Message message = createMessage();

        // Create navwarn message
        message = messageService.create(message);
        log.info("NavwarnMessage created with id: " + message.getId());
        assertNotNull(message.getId());

        // Index the message
        assertEquals(1, messageSearchService.updateLuceneIndex());

        // Search the index
        assertEquals(1, messageSearchService.search(new MessageSearchParams("Kattegat", null)).getMessages().size());

        // Search on geometry
        Location location = new Location();
        location.setType(Location.LocationType.CIRCLE);
        location.getPoints().add(new Point(location, 56.120, 12.1684));
        location.setRadius(10);
        assertEquals(1, messageSearchService.search(new MessageSearchParams(null, location)).getMessages().size());

        messageService.remove(message);
    }



    public static Message createMessage() throws ParseException {
        Message message = new Message();

        message.setStatus(Status.PUBLISHED);
        message.setType(Type.NAVAREA_WARNING);

        // Message series identifier
        SeriesIdentifier identifier = new SeriesIdentifier();
        identifier.setMainType(SeriesIdType.MSI);
        identifier.setAuthority("DMA");
        identifier.setYear(2013);
        identifier.setNumber(new Random(System.currentTimeMillis()).nextInt(1000) + 1);

        // Tie to message
        message.setSeriesIdentifier(identifier);
        //identifier.setMessage(message);

        // MessageDescs 's
        MessageDesc desc = message.createDesc("en");
        desc.setTitle("Bridge has collapsed");
        desc.setVicinity("Kattegat");
        desc.setDescription("Debris in water");

        // Locations
        Location loc1 = new Location(Location.LocationType.POLYGON);
        loc1.addPoint(new Point(loc1, 56.120, 12.1684));
        loc1.addPoint(new Point(loc1, 55.877, 12.622));
        loc1.addPoint(new Point(loc1, 55.962, 12.576));
        message.getLocations().add(loc1);
        Location loc2 = new Location(Location.LocationType.POLYGON);
        loc2.addPoint(new Point(loc2, 57.120, 13.1684));
        loc2.addPoint(new Point(loc2, 57.877, 13.622));
        loc2.addPoint(new Point(loc2, 57.962, 13.576));
        message.getLocations().add(loc2);

        return message;
    }
}
