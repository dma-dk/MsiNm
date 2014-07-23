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
import dk.dma.msinm.common.sequence.Sequences;
import dk.dma.msinm.common.service.BaseService;
import dk.dma.msinm.legacy.msi.model.LegacyMessage;
import dk.dma.msinm.model.Category;
import dk.dma.msinm.model.Location;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.service.AreaService;
import dk.dma.msinm.service.CategoryService;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

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

    @Inject
    CategoryService categoryService;

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
     * Saves the legacy message in a new transaction
     * @param legacyMessage the legacy message to save
     * @return the result
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public LegacyMessage saveLegacyMessage(LegacyMessage legacyMessage) {

        try {
            Message message = legacyMessage.getMessage();

            // Check the location to make it valid
            Location loc = message.getLocations().get(0);
            if (loc != null && loc.getType() == Location.LocationType.POLYGON && loc.getPoints().size() < 3) {
                loc.setType(Location.LocationType.POLYLINE);
            }

            // Substitute the template area with a persisted one
            message.setArea(areaService.findOrCreateArea(message.getArea()));

            // Substitute the template category with a persisted one
            if (!message.getCategories().isEmpty()) {
                Category category = categoryService.findOrCreateCategory(message.getCategories().get(0));
                message.getCategories().clear();
                if (category != null) {
                    message.getCategories().add(category);
                }
            }

            legacyMessage = saveEntity(legacyMessage);
            log.info("Persisted legacy message " + legacyMessage);
        } catch (Exception e) {
            log.error("Error importing legacy message " + legacyMessage.getLegacyId(), e);
        }

        return legacyMessage;
    }
}
