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

import dk.dma.msinm.common.MsiNmApp;
import dk.dma.msinm.common.sequence.Sequences;
import dk.dma.msinm.common.service.BaseService;
import dk.dma.msinm.legacy.model.LegacyMessage;
import dk.dma.msinm.model.*;
import dk.dma.msinm.service.AreaService;
import dk.frv.msiedit.core.webservice.message.MsiDto;
import dk.frv.msiedit.core.webservice.message.PointDto;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Provides an interface for persisting and fetching legacy Danish MSI messages
 */
@Stateless
public class LegacyMessageService extends BaseService {

    @Inject
    private Logger log;

    @Inject
    Sequences sequences;

    @Inject
    MsiNmApp app;

    @Inject
    AreaService areaService;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public LegacyMessage saveLegacyMessage(LegacyMessage legacyMessage) {
        return saveEntity(legacyMessage);
    }

    /**
     * Looks for a LegacyMessage with the given id. Returns null if not found
     * @param legacyId the id of the LegacyMessage to search for
     * @return the LegacyMessage or null
     */
    public LegacyMessage findByLegacyId(Integer legacyId) {
        try {
            // Look up a  matching LegacyMessage
            return em
                    .createNamedQuery("LegacyMessage.findByLegacyId", LegacyMessage.class)
                    .setParameter("legacyId", legacyId)
                    .getSingleResult();

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Create and persist a new MSI warning from a legacy warning
     *
     * @param msi the legacy warning
     * @return if the warning caused a new message or an update
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public boolean createOrUpdateNavwarnMessage(MsiDto msi) {

        LegacyMessage legacyMessage = findByLegacyId(msi.getId());

        if (legacyMessage == null) {
            // No matching LegacyMessage found. Create one
            legacyMessage = new LegacyMessage();
            legacyMessage.setLegacyId(msi.getId());
            legacyMessage.setNavtexNo(msi.getNavtexNo());
            legacyMessage.setVersion(msi.getVersion());

            Message message = new Message();
            message.setStatus(MessageStatus.ACTIVE);
            legacyMessage.setMessage(message);
        } else if (legacyMessage.getVersion() >= msi.getVersion()) {
            // No further updates...
            return false;
        }

        // Update the message from the legacy MSI warning
        updateMessage(msi, legacyMessage.getMessage());

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
    private void updateMessage(MsiDto msi, Message message) {
        // Message series identifier
        MessageSeriesIdentifier identifier = message.getSeriesIdentifier();
        if (identifier == null) {
            identifier = new MessageSeriesIdentifier();
            message.setSeriesIdentifier(identifier);
            identifier.setNumber((int) sequences.getNextValue(Message.MESSAGE_SEQUENCE));
            identifier.setAuthority(msi.getOrganisation());
            identifier.setYear(msi.getCreated().toGregorianCalendar().get(Calendar.YEAR));
            identifier.setType(MessageType.NAVAREA_WARNING);
        }

        // Area
        // Find Denmark
        Area area = areaService.findByName("denmark", "en", null);
        if (area == null) {
            area = new Area();
            area.createDesc("en").setName("Denmark");
            area.createDesc("da").setName("Danmark");
            area = areaService.createArea(area, null);
        }
        Integer parentId = area.getId();

        if (StringUtils.isNotBlank(msi.getAreaEnglish())) {
            area = areaService.findByName(msi.getAreaEnglish(), "en", parentId);
            if (area == null) {
                area = new Area();
                area.createDesc("en").setName(msi.getAreaEnglish());
                area.createDesc("da").setName(msi.getAreaEnglish());
                area = areaService.createArea(area, parentId);
            }
            parentId = area.getId();
        }

        if (StringUtils.isNotBlank(msi.getSubarea())) {
            area = areaService.findByName(msi.getAreaEnglish(), "en", parentId);
            if (area == null) {
                area = new Area();
                area.createDesc("en").setName(msi.getSubarea());
                area.createDesc("da").setName(msi.getSubarea());
                area = areaService.createArea(area, parentId);
            }
        }
        message.setArea(area);

        // Dates
        message.setValidFrom(msi.getValidFrom().toGregorianCalendar().getTime());
        if (msi.getValidTo() != null) {
            message.setValidFrom(msi.getValidFrom().toGregorianCalendar().getTime());
        }

        if (msi.getDeleted() != null) {
            message.setCancellationDate(msi.getDeleted().toGregorianCalendar().getTime());
        } else {
            message.setCancellationDate(null);
        }

        // Localized contents - updated da = en
        Arrays.asList(app.getLanguages()).forEach(message::createDesc);
        message.getDescs().forEach(desc -> {
            desc.setTitle(StringUtils.defaultString(msi.getEncText(), msi.getNavWarning()));
            desc.setDescription(msi.getNavWarning());
        });

        message.getLocations().clear();
        if (msi.getPoints() != null && msi.getPoints().getPoint().size() > 0) {
            Location.LocationType type;
            switch (msi.getLocationType()) {
                case "Point":       type = Location.LocationType.POINT; break;
                case "Polygon":     type = Location.LocationType.POLYGON; break;
                case "Points":      type = Location.LocationType.POLYLINE; break;
                case "Polyline":    type = Location.LocationType.POLYLINE; break;
                default:            type = Location.LocationType.POLYLINE;
            }
            Location loc1 = new Location(type);
            for (PointDto p : msi.getPoints().getPoint()) {
                loc1.addPoint(new Point(loc1, p.getLatitude(), p.getLongitude(), p.getPtno()));
            }
            message.getLocations().add(loc1);
        }
    }

}
