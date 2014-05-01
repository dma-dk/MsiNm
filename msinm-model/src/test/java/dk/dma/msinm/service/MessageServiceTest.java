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
import dk.dma.msinm.common.db.DatabaseConfiguration;
import dk.dma.msinm.common.sequence.SequenceEntity;
import dk.dma.msinm.common.sequence.Sequences;
import dk.dma.msinm.common.settings.Settings;
import dk.dma.msinm.common.settings.SettingsEntity;
import dk.dma.msinm.model.*;
import dk.dma.msinm.test.MsiNmUnitTest;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for the MessageService
 */
@RunWith(CdiRunner.class)
@AdditionalClasses(value = {
        DatabaseConfiguration.class, Settings.class,
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
                Message.class, MessageCategory.class, MessageItem.class, MessageLocation.class,
                MessageSeriesIdentifier.class, NavwarnMessage.class, NoticeElement.class,
                NoticeMessage.class, PermanentItem.class, Point.class, TempPreliminaryItem.class
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

        NavwarnMessage message = createNavwarnMessage();

        // Create navwarn message
        message = messageService.create(message);
        log.info("NavwarnMessage created with id: " + message.getId());
        assertNotNull(message.getId());

        // Index the message
        assertEquals(1, messageSearchService.updateLuceneIndex());

        // Search the index
        assertEquals(1, messageSearchService.search(new MessageSearchParams("Kattegat", null)).size());

        // Search on geometry
        MessageLocation location = new MessageLocation();
        location.setType(MessageLocation.LocationType.CIRCLE);
        location.getPoints().add(new Point(56.120, 12.1684));
        location.setRadius(10);
        assertEquals(1, messageSearchService.search(new MessageSearchParams(null, location)).size());

    }

    @Test
    public void testNoticeMessage() throws Exception {
        NoticeMessage message = createNoticeMessage();

        message = messageService.create(message);
        log.info("NavwarnMessage created with id: " + message.getId());
        assertNotNull(message.getId());

        // Index the message
        assertEquals(1, messageSearchService.updateLuceneIndex());

        // Search the index
        assertEquals(1, messageSearchService.search(new MessageSearchParams("Langebro", null)).size());

        // Search on geometry
        MessageLocation location = new MessageLocation();
        location.setType(MessageLocation.LocationType.CIRCLE);
        location.getPoints().add(new Point(57, 12));
        location.setRadius(10);
        assertEquals(1, messageSearchService.search(new MessageSearchParams(null, location)).size());
    }


    public static NavwarnMessage createNavwarnMessage() throws ParseException {
        NavwarnMessage message = new NavwarnMessage();

        message.setStatus(MessageStatus.ACTIVE);

        // Message series identifier
        MessageSeriesIdentifier identifier = new MessageSeriesIdentifier();
        identifier.setAuthority("DMA");
        identifier.setYear(2013);
        identifier.setNumber(new Random(System.currentTimeMillis()).nextInt(1000) + 1);
        identifier.setType(MessageType.NAVAREA_WARNING);

        // Tie to message
        message.setSeriesIdentifier(identifier);
        //identifier.setMessage(message);

        // Message
        message.setGeneralArea("Kattegat");
        message.setLocality("The Sound");
        message.getSpecificLocations().add("Copenhagen port");
        message.getSpecificLocations().add("Langebro bridge");
        message.getChartNumbers().add("daddasd");
        message.getIntChartNumbers().add(100);
        message.setIssueDate(new Date(System.currentTimeMillis()));

        // NavwarnMessage
        message.setCancellationDate((new SimpleDateFormat("dd-MM-yyyy")).parse("31-12-2013"));

        // MessageItem 's
        MessageItem item1 = new MessageItem();
        item1.setKeySubject("Bridge has collapsed");
        item1.setAmplifyingRemarks("Debris in water");

        MessageCategory cat1 = new MessageCategory();
        cat1.setGeneralCategory(GeneralCategory.AIDS_TO_NAVIGATION);
        cat1.setSpecificCategory(SpecificCategory.BUOY);
        cat1.setOtherCategory("Unlit");
        item1.setCategory(cat1);

        MessageLocation loc1 = new MessageLocation(MessageLocation.LocationType.POLYGON);
        loc1.addPoint(new Point(56.120, 12.1684));
        loc1.addPoint(new Point(55.877, 12.622));
        loc1.addPoint(new Point(55.962, 12.576));
        item1.getLocations().add(loc1);
        MessageLocation loc2 = new MessageLocation(MessageLocation.LocationType.POLYGON);
        loc2.addPoint(new Point(57.120, 13.1684));
        loc2.addPoint(new Point(57.877, 13.622));
        loc2.addPoint(new Point(57.962, 13.576));
        item1.getLocations().add(loc2);

        MessageItem item2 = new MessageItem();
        item2.setKeySubject("Plane crash");
        item2.setAmplifyingRemarks("Debris in water");
        MessageCategory cat2 = new MessageCategory();
        cat2.setGeneralCategory(GeneralCategory.DANGEROUS_WRECKS);
        cat2.setSpecificCategory(SpecificCategory.WRECK_AND_GROUNDS);
        cat2.setOtherCategory("Adrift");
        item2.setCategory(cat2);

        // Tie message items to navwarn message
        message.getMessageItems().add(item1);
        message.getMessageItems().add(item2);
        return message;
    }

    public static NoticeMessage createNoticeMessage() {
        NoticeMessage message = new NoticeMessage();

        message.setStatus(MessageStatus.ACTIVE);

        // Message series identifier
        MessageSeriesIdentifier identifier = new MessageSeriesIdentifier();
        identifier.setAuthority("DMA");
        identifier.setYear(2013);
        identifier.setNumber(new Random(System.currentTimeMillis()).nextInt(1000) + 1);
        identifier.setType(MessageType.TEMPORARY_NOTICE);

        // Tie to message
        message.setSeriesIdentifier(identifier);
        //identifier.setMessage(message);

        // Message
        message.setGeneralArea("Kattegat");
        message.setLocality("The Sound");
        message.getSpecificLocations().add("Copenhagen port");
        message.getSpecificLocations().add("Langebro bridge");
        message.getChartNumbers().add("daddasd");
        message.getIntChartNumbers().add(100);
        message.setIssueDate(new Date(System.currentTimeMillis()));

        // Notice message specifics
        message.setAuthority("DMA");
        message.getLightsListNumbers().add("DA-213123");
        message.setAmplifyingRemarks("Avoid any entry");

        PermanentItem permanentItem = new PermanentItem();
        permanentItem.setAmplifyingRemarks("Amplifying remarks");
        permanentItem.setChartNumber("DK-1213");
        permanentItem.setHorizontalDatum("FSdasd");
        MessageLocation pitLoc = new MessageLocation(MessageLocation.LocationType.POINT);
        pitLoc.addPoint(new Point(57, 12));
        permanentItem.setLocation(pitLoc);
        permanentItem.setLastUpdate(identifier);

        NoticeElement noticeElement = new NoticeElement();
        noticeElement.setAmplifyingNote("Hello world");
        noticeElement.setFeatureOrCharacteristic("Bridge");
        noticeElement.getGraphicalRepresentations().add("sadasd");
        noticeElement.getGraphicalRepresentations().add("basasdas");
        noticeElement.setLocation(pitLoc);
        noticeElement.setNoticeVerb(NoticeVerb.REPLACE);

        permanentItem.getNoticeElements().add(noticeElement);

        message.getPermanentItems().add(permanentItem);

        TempPreliminaryItem preliminaryItem = new TempPreliminaryItem();
        preliminaryItem.setLocation(pitLoc);
        preliminaryItem.getGraphicalRepresentations().add("sdad");
        preliminaryItem.setItemDescription("dasdadd");

        message.getTempPreliminaryItems().add(preliminaryItem);
        return message;
    }

}
