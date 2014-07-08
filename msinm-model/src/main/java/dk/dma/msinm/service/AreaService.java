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

import dk.dma.msinm.common.MsiNmApp;
import dk.dma.msinm.common.db.PredicateHelper;
import dk.dma.msinm.common.service.BaseService;
import dk.dma.msinm.model.Area;
import dk.dma.msinm.model.AreaDesc;
import dk.dma.msinm.vo.AreaVo;
import dk.dma.msinm.vo.CopyOp;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Business interface for accessing MSI-NM areas
 */
@Stateless
public class AreaService extends BaseService {

    @Inject
    private Logger log;

    @Inject
    private MsiNmApp app;

    /**
     * Searchs for areas matching the given term in the given language
     * @param lang the language
     * @param term the search term
     * @param limit the maximum number of results
     * @return the search result
     */
    public List<AreaVo> searchAreas(String lang, String term, int limit) {
        List<AreaVo> result = new ArrayList<>();
        if (StringUtils.isNotBlank(term)) {
            List<Area> areas = em
                    .createNamedQuery("Area.searchAreas", Area.class)
                    .setParameter("lang", lang)
                    .setParameter("term", "%" + term + "%")
                    .setParameter("sort", term)
                    .setMaxResults(limit)
                    .getResultList();

            CopyOp copyOp = CopyOp.get(CopyOp.PARENT).setLang(lang);
            areas.forEach(area -> result.add(new AreaVo(area, copyOp)));
        }
        return result;
    }

    /**
     * Returns the hierarchical list of root areas.
     * <p></p>
     * The returned list is a condensed data set where only description records
     * for the given language is included and no locations are included.
     *
     * @param lang the language
     * @return the hierarchical list of root areas
     */
    public List<AreaVo> getAreaTreeForLanguage(String lang) {
        // Ensure validity
        final String language = app.getLanguage(lang);

        // Get all areas along with their AreaDesc records
        // Will ensure that all Area entities are cached in the entity manager before organizing the result
        List<Area> areas = em
                .createNamedQuery("Area.findAreasWithDescs", Area.class)
                .getResultList();

        // Create a lookup map
        //Map<Integer, AreaVo> areaLookup = areas.stream()
        //        .collect(Collectors.toMap(Area::getId, area -> new AreaVo(area, language)));
        Map<Integer, AreaVo> areaLookup = new HashMap<>();
        areas.stream()
                .forEach(area -> areaLookup.put(area.getId(), new AreaVo(area, CopyOp.get(CopyOp.PARENT_ID).setLang(language))));


        // Add non-roots as child areas to their parent area
        areaLookup.values().stream()
                .filter(areaVo -> areaVo.getParent() != null)
                .forEach(areaVo -> areaLookup.get(areaVo.getParent().getId()).getChildren().add(areaVo));

        // Return roots
        return areaLookup.values().stream()
                .filter(areaVo -> areaVo.getParent() == null)
                .collect(Collectors.toList());
    }

    /**
     * Looks up an area and the associated data, but does NOT look up
     * the child-area hierarchy
     *
     * @param id the id of the area
     * @return the area
     */
    public AreaVo getAreaDetails(Integer id) {
        Area area = getByPrimaryKey(Area.class, id);
        if (area == null) {
            return null;
        }

        // NB: No child areas included
        return new AreaVo(area, CopyOp.get("locations"));
    }

    /**
     * Updates the area data from the area template, but not the parent-child hierarchy of the area
     * @param area the area to update
     * @return the updated area
     */
    public Area updateAreaData(Area area) {
        Area original = getByPrimaryKey(Area.class, area.getId());

        // Copy the area data
        original.copyDescs(area.getDescs());

        // Add the locations
        original.getLocations().clear();
        original.getLocations().addAll(area.getLocations());

        // Update lineage
        original.updateLineage();

        return saveEntity(original);
    }

    /**
     * Creates a new area based on the area template
     * @param area the area to create
     * @param parentId the id of the parent area
     * @return the created area
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Area createArea(Area area, Integer parentId) {

        if (parentId != null) {
            Area parent = getByPrimaryKey(Area.class, parentId);
            parent.getChildren().add(area);
            area.setParent(parent);
        }

        area = saveEntity(area);

        // The area now has an ID - Update lineage
        area.updateLineage();
        area = saveEntity(area);

        em.flush();
        return area;
    }

    /**
     * Moves the area to the given parent id
     * @param areaId the id of the area to create
     * @param parentId the id of the parent area
     * @return the updated area
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Area moveArea(Integer areaId, Integer parentId) {
        Area area = getByPrimaryKey(Area.class, areaId);

        if (area.getParent() != null && !area.getParent().getId().equals(parentId)) {
            area.getParent().getChildren().remove(area);
        }

        if (parentId == null) {
            area.setParent(null);
        } else {
            Area parent = getByPrimaryKey(Area.class, parentId);
            area.setParent(parent);
            parent.getChildren().add(area);
        }

        // Save the entity
        area = saveEntity(area);
        em.flush();

        // Update all lineages
        updateLineages();

        // Return the update area
        return getByPrimaryKey(Area.class, area.getId());
    }

    /**
     * Update lineages for all areas
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void updateLineages() {

        log.info("Update area lineages");

        // Get root areas
        List<Area> roots = getAll(Area.class).stream()
            .filter(Area::isRootArea)
            .collect(Collectors.toList());

        // Update each root subtree
        List<Area> updated = new ArrayList<>();
        roots.forEach(area -> updateLineages(area, updated));

        // Persist the changes
        updated.forEach(this::saveEntity);
        em.flush();
    }

    /**
     * Recursively updates the lineages of areas rooted at the given area
     * @param area the area whose sub-tree should be updated
     * @param areas the list of updated areas
     * @return if the lineage was updated
     */
    private boolean updateLineages(Area area, List<Area> areas) {

        boolean updated = area.updateLineage();
        if (updated) {
            areas.add(area);
        }
        area.getChildren().forEach(childArea -> updateLineages(childArea, areas));
        return updated;
    }

    /**
     * Deletes the area and sub-areas
     * @param areaId the id of the area to delete
     */
    public boolean deleteArea(Integer areaId) {

        Area area = getByPrimaryKey(Area.class, areaId);
        if (area != null) {
            area.setParent(null);
            saveEntity(area);
            remove(area);
            return true;
        }
        return false;
    }


    /**
     * Looks up an area by name
     * @param name the name to search for
     * @param lang the language. Optional
     * @param parentId the parent ID. Optional
     * @return The matching area, or null if not found
     */
    public Area findByName(String name, String lang, Integer parentId) {
        // Sanity check
        if (StringUtils.isBlank(name)) {
            return null;
        }

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Area> areaQuery = builder.createQuery(Area.class);

        Root<Area> areaRoot = areaQuery.from(Area.class);

        // Build the predicate
        PredicateHelper<Area> predicateBuilder = new PredicateHelper<>(builder, areaQuery);

        // Match the name
        Join<Area, AreaDesc> descs = areaRoot.join("descs", JoinType.LEFT);
        predicateBuilder.like(descs.get("name"), name);
        // Optionally, match the language
        if (StringUtils.isNotBlank(lang)) {
            predicateBuilder.equals(descs.get("lang"), lang);
        }

        // Optionally, match the parent
        if (parentId != null) {
            areaRoot.join("parent", JoinType.LEFT);
            Path<Area> parent = areaRoot.get("parent");
            predicateBuilder.equals(parent.get("id"), parentId);
        }

        // Complete the query
        areaQuery.select(areaRoot)
                .distinct(true)
                .where(predicateBuilder.where());

        // Execute the query and update the search result
        List<Area> result = em
                .createQuery(areaQuery)
                .getResultList();

        return result.size() > 0 ? result.get(0) : null;
    }


}
