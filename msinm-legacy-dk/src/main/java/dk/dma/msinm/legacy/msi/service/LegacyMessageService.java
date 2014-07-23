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
import dk.dma.msinm.model.Area;
import dk.dma.msinm.model.Category;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.service.AreaService;
import dk.dma.msinm.service.CategoryService;
import org.apache.commons.lang.StringUtils;
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
     * Saves the legacy message in a new transaction
     * @param legacyMessage the legacy message to save
     * @return the result
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public LegacyMessage saveLegacyMessage(LegacyMessage legacyMessage) {

        // We don't want to pass detached entities to persist. Refresh area and category
        Message message = legacyMessage.getMessage();

        Area area = getByPrimaryKey(Area.class, message.getArea().getId());
        message.setArea(area);

        if (message.getCategories().size() > 0) {
            Category category = getByPrimaryKey(Category.class, message.getCategories().get(0).getId());
            message.getCategories().clear();
            message.getCategories().add(category);
        }

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
     * Looks up or creates an Area with the given name under the given parent Area
     * @param nameEn the english name
     * @param nameDa the Danish name
     * @param parent the parent Area
     * @return the Area
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Area findOrCreateArea(String nameEn, String nameDa, Area parent) {
        Integer parentId = (parent == null) ? null : parent.getId();

        if (StringUtils.isNotBlank(nameEn) || StringUtils.isNotBlank(nameDa)) {
            Area area = areaService.findByName(nameEn, "en", parentId);
            if (area == null) {
                area = areaService.findByName(nameDa, "da", parentId);
            }
            if (area == null) {
                area = new Area();
                if (StringUtils.isNotBlank(nameEn)) {
                    area.createDesc("en").setName(nameEn);
                }
                if (StringUtils.isNotBlank(nameDa)) {
                    area.createDesc("da").setName(nameDa);
                }
                area = areaService.createArea(area, parentId);
                log.info("Created area " + area);
            }
            return area;
        }
        return parent;
    }

    /**
     * Looks up or creates a Category with the given name under the given parent Category
     * @param nameEn the english name
     * @param nameDa the Danish name
     * @param parent the parent Category
     * @return the Category
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Category findOrCreateCategory(String nameEn, String nameDa, Category parent) {
        Integer parentId = (parent == null) ? null : parent.getId();

        if (StringUtils.isNotBlank(nameEn) || StringUtils.isNotBlank(nameDa)) {
            Category category = categoryService.findByName(nameEn, "en", parentId);
            if (category == null) {
                category = categoryService.findByName(nameDa, "da", parentId);
            }
            if (category == null) {
                category = new Category();
                if (StringUtils.isNotBlank(nameEn)) {
                    category.createDesc("en").setName(nameEn);
                }
                if (StringUtils.isNotBlank(nameDa)) {
                    category.createDesc("da").setName(nameDa);
                }
                category = categoryService.createCategory(category, parentId);
                log.info("Created category " + category);
            }
            return category;
        }
        return parent;
    }

}
