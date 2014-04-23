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

import dk.dma.msinm.common.service.BaseService;
import dk.dma.msinm.common.service.SequenceService;
import dk.dma.msinm.legacy.model.LegacyMessage;
import dk.dma.msinm.model.*;
import dk.frv.msiedit.core.webservice.message.MsiDto;
import dk.frv.msiedit.core.webservice.message.PointDto;
import org.slf4j.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.Calendar;

/**
 * Provides an interface for persisting and fetching legacy MSI messages
 */
@Stateless
public class LegacyMessageService extends BaseService {

    @Inject
    private Logger log;

    @EJB
    SequenceService sequenceService;

    /**
     * Create and persist a new MSI warning from a legacy warning
     *
     * @param msi the legacy warning
     * @return if the warning caused a new message or an update
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public boolean createOrUpdateNavwarnMessage(MsiDto msi) {

        LegacyMessage legacyMessage = null;
        try {
            // Look up a  matching LegacyMessage
            legacyMessage = em
                    .createNamedQuery("LegacyMessage.findByMessageId", LegacyMessage.class)
                    .setParameter("messageId", msi.getMessageId())
                    .getSingleResult();
        
            // Check if the message has been updated
            if (legacyMessage.getVersion() >= msi.getVersion()) {
                // No further updates...
                return false;
            }

        } catch (Exception e) {
            // No matching LegacyMessage found. Create one
            legacyMessage = new LegacyMessage();
            legacyMessage.setMessageId(msi.getMessageId());
            legacyMessage.setNavtexNo(msi.getNavtexNo());
            legacyMessage.setVersion(msi.getVersion());

            NavwarnMessage message = new NavwarnMessage();
            legacyMessage.setNavwarnMessage(message);
        }

        // Update the message from the legacy MSI warning
        updateMessage(msi, legacyMessage.getNavwarnMessage());

        // Save the new entity
        try {
            saveEntity(legacyMessage);
            log.info("Persisted new or updated legacy MSI as navwarn message " + msi);
        } catch (Exception ex) {
            log.error("Error persisting legacy MSI " + msi, ex);
            return false;
        }

        return true;
    }

    /**
     *  Update the message from the legacy MSI warning
     * @param msi the legacy warning
     * @param message the message
     */
    private void updateMessage(MsiDto msi, NavwarnMessage message) {
        // Message series identifier
        MessageSeriesIdentifier identifier = message.getSeriesIdentifier();
        if (identifier == null) {
            identifier = new MessageSeriesIdentifier();
            message.setSeriesIdentifier(identifier);
        }
        identifier.setAuthority(msi.getOrganisation());
        identifier.setYear(msi.getCreated().toGregorianCalendar().get(Calendar.YEAR));
        identifier.setNumber((int) sequenceService.getNextValue(Message.MESSAGE_SEQUENCE));
        identifier.setType(MessageType.NAVAREA_WARNING);

        // Message
        message.setGeneralArea(msi.getAreaEnglish());
        message.setLocality(msi.getSubarea());
        message.setIssueDate(msi.getValidFrom().toGregorianCalendar().getTime());

        // NavwarnMessage
        if (msi.getDeleted() != null) {
            message.setCancellationDate(msi.getDeleted().toGregorianCalendar().getTime());
        } else {
            message.setCancellationDate(null);
        }

        // MessageItem 's
        MessageItem item1;
        if (message.getMessageItems().size() == 0) {
            item1 = new MessageItem();
            message.getMessageItems().add(item1);

            MessageCategory cat1 = new MessageCategory();
            cat1.setGeneralCategory(GeneralCategory.ISOLATED_DANGERS);
            cat1.setSpecificCategory(SpecificCategory.OBSTRUCTION);
            cat1.setOtherCategory("");
            item1.setCategory(cat1);
        } else {
            item1 = message.getMessageItems().get(0);
        }
        item1.setKeySubject(msi.getNavWarning());
        item1.setAmplifyingRemarks(msi.getEncText());

        item1.getLocations().clear();
        if (msi.getPoints() != null && msi.getPoints().getPoint().size() > 0) {
            MessageLocation loc1 = new MessageLocation(MessageLocation.LocationType.POLYGON);
            for (PointDto p : msi.getPoints().getPoint()) {
                loc1.addPoint(new Point(p.getLatitude(), p.getLongitude()));
            }
            item1.getLocations().add(loc1);
        }
    }

}
