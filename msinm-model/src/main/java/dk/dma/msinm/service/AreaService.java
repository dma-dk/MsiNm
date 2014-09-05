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
import dk.dma.msinm.common.db.Sql;
import dk.dma.msinm.common.model.DataFilter;
import dk.dma.msinm.common.service.BaseService;
import dk.dma.msinm.common.settings.DefaultSetting;
import dk.dma.msinm.common.settings.Setting;
import dk.dma.msinm.common.settings.Settings;
import dk.dma.msinm.common.settings.SettingsEntity;
import dk.dma.msinm.model.Area;
import dk.dma.msinm.model.AreaDesc;
import dk.dma.msinm.vo.AreaVo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Business interface for accessing MSI-NM areas
 */
@Stateless
public class AreaService extends BaseService {

    // Used by the recomputeTreeSortOrder() method
    public static final Setting AREA_LAST_UPDATE = new DefaultSetting("areaLastUpdate", "0");

    @Inject
    private Logger log;

    @Inject
    MessageService messageService;

    @Inject
    private MsiNmApp app;

    @Inject
    @Sql("/sql/area_messages.sql")
    private String areaMessagesSql;

    @Inject
    private Settings settings;

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

            DataFilter dataFilter = DataFilter.get(DataFilter.PARENT).setLang(lang);
            areas.forEach(area -> result.add(new AreaVo(area, dataFilter)));
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
                .forEach(area -> areaLookup.put(area.getId(), new AreaVo(area, DataFilter.get(DataFilter.PARENT_ID).setLang(language))));


        // Add non-roots as child areas to their parent area
        areaLookup.values().stream()
                .filter(areaVo -> areaVo.getParent() != null)
                .forEach(areaVo -> areaLookup.get(areaVo.getParent().getId()).checkCreateChildren().add(areaVo));

        // Return roots
        List<AreaVo> roots = areaLookup.values().stream()
                .filter(areaVo -> areaVo.getParent() == null)
                .collect(Collectors.toList());

        // Sort the trees according to sort order
        Collections.sort(roots);
        roots.forEach(AreaVo::sortChildren);

        return roots;
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
        return new AreaVo(area, DataFilter.get("locations"));
    }

    /**
     * Updates the area data from the area template, but not the parent-child hierarchy of the area
     * @param area the area to update
     * @return the updated area
     */
    public Area updateAreaData(Area area) {
        Area original = getByPrimaryKey(Area.class, area.getId());

        original.setSortOrder(area.getSortOrder());

        // Copy the area data
        original.copyDescsAndRemoveBlanks(area.getDescs());

        // Add the locations
        original.getLocations().clear();
        original.getLocations().addAll(area.getLocations());

        // Update lineage
        original.updateLineage();

        original = saveEntity(original);

        // Evict all cached messages for the area subtree
        evictCachedMessages(original);

        return original;
    }

    /**
     * Creates a new area based on the area template
     * @param area the area to create
     * @param parentId the id of the parent area
     * @return the created area
     */
    public Area createArea(Area area, Integer parentId) {

        if (parentId != null) {
            Area parent = getByPrimaryKey(Area.class, parentId);
            parent.addChild(area);
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
    public Area moveArea(Integer areaId, Integer parentId) {
        Area area = getByPrimaryKey(Area.class, areaId);

        if (area.getParent() != null && !area.getParent().getId().equals(parentId)) {
            area.getParent().getChildren().remove(area);
        }

        if (parentId == null) {
            area.setParent(null);
        } else {
            Area parent = getByPrimaryKey(Area.class, parentId);
            parent.addChild(area);
        }

        // Save the entity
        area = saveEntity(area);
        em.flush();

        // Update all lineages
        updateLineages();

        // Return the update area
        area = getByPrimaryKey(Area.class, area.getId());

        // Evict all cached messages for the area subtree
        evictCachedMessages(area);

        return area;
    }

    /**
     * Changes the sort order of an area, by moving it up or down compared to siblings.
     * <p>
     * Please note that by moving "up" we mean in a geographical tree structure,
     * i.e. a smaller sortOrder value.
     *
     * @param areaId the id of the area to move
     * @param moveUp whether to move the area up or down
     * @return the updated area
     */
    public Area changeSortOrder(Integer areaId, boolean moveUp) {
        Area area = getByPrimaryKey(Area.class, areaId);
        boolean updated = false;

        // Non-root case
        if (area.getParent() != null) {
            List<Area> siblings = area.getParent().getChildren();
            int index = siblings.indexOf(area);

            if (moveUp) {
                if (index == 1) {
                    area.setSortOrder(siblings.get(0).getSortOrder() - 10.0);
                    updated = true;
                } else if (index > 1) {
                    double so1 =  siblings.get(index - 1).getSortOrder();
                    double so2 =  siblings.get(index - 2).getSortOrder();
                    area.setSortOrder((so1 + so2) / 2.0);
                    updated = true;
                }

            } else {
                if (index == siblings.size() - 2) {
                    area.setSortOrder(siblings.get(siblings.size() - 1).getSortOrder() + 10.0);
                    updated = true;
                } else if (index < siblings.size() - 2) {
                    double so1 =  siblings.get(index + 1).getSortOrder();
                    double so2 =  siblings.get(index + 2).getSortOrder();
                    area.setSortOrder((so1 + so2) / 2.0);
                    updated = true;
                }

            }

        } else {
            // TODO root case
        }

        if (updated) {
            log.info("Updates sort order for area " + area.getId() + " to " + area.getSortOrder());
            // Save the entity
            area = saveEntity(area);

            // NB: Cache eviction not needed since lineage is the same...
        }

        return area;
    }

    /**
     * Evict all cached messages for the given subtree of areas
     * @param area the subtree to evict cacahed messaged for
     */
    private void evictCachedMessages(Area area) {
        // Sanity check
        if (area == null || area.getLineage() == null) {
            return;
        }

        String sql = areaMessagesSql.replace(":lineage", "'" + area.getLineage() + "%'");

        List<?> ids = em.createNativeQuery(sql)
                .getResultList();

        ids.forEach(o -> messageService.evictCachedMessageId(((Number) o).intValue()));
    }

    /**
     * Update lineages for all areas
     */
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

    /**
     * Ensures that the template area and it's parents exists
     * @param templateArea the template area
     * @return the area
     */
    public Area findOrCreateArea(Area templateArea) {
        // Sanity checks
        if (templateArea == null || templateArea.getDescs().size() == 0) {
            return null;
        }

        // Recursively, resolve the parent areas
        Area parent = null;
        if (templateArea.getParent() != null) {
            parent = findOrCreateArea(templateArea.getParent());
        }
        Integer parentId = (parent == null) ? null : parent.getId();

        // Check if we can find the given area
        Area area = null;
        for (int x = 0; area == null && x < templateArea.getDescs().size(); x++) {
            AreaDesc desc = templateArea.getDescs().get(x);
            area = findByName(desc.getName(), desc.getLang(), parentId);
        }

        // Create the area if no matching area was found
        if (area == null) {
            area = createArea(templateArea, parentId);
        }
        return area;
    }

    /**
     * Returns the last change date for areas or null if no area exists
     * @return the last change date for areas
     */
    public Date getLastUpdated() {
        try {
            return em.createQuery("select max(a.updated) from Area a", Date.class).getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Potentially, a heavy-duty function that scans the entire area tree,
     * sorts it and update the treeSortOrder. Use with care.
     */
    public void recomputeTreeSortOrder() {
        long t0 = System.currentTimeMillis();

        // Compare the last area update date and the last processed date
        Date lastAreaUpdate = getLastUpdated();
        if (lastAreaUpdate == null) {
            // No areas
            return;
        }

        Date lastProcessedUpdate = settings.getDate(AREA_LAST_UPDATE);
        if (!lastAreaUpdate.after(lastProcessedUpdate)) {
            log.info("No area tree changes since last execution of recomputeTreeSortOrder()");
            return;
        }

        List<Area> roots = em
                .createNamedQuery("Area.findRootAreas", Area.class)
                .getResultList();

        // Sort the roots by sortOrder
        Collections.sort(roots);

        // Re-compute the tree sort order
        List<Area> updated = new ArrayList<>();
        recomputeTreeSortOrder(roots, 0, updated, false);

        // Persist changed areas
        updated.forEach(this::saveEntity);

        em.flush();

        // Update the last processed date
        settings.updateSetting(new SettingsEntity(
                AREA_LAST_UPDATE.getSettingName(),
                String.valueOf(System.currentTimeMillis() + 1000)));

        log.info("Recomputed tree sort order in " + (System.currentTimeMillis() - t0) + " ms");
    }

    /**
     * Recursively recomputes the treeSortOrder, by enumerating the sorted area list and their children
     * @param areas the list of areas to update
     * @param index the current area index
     * @param updatedAreas the list of updated areas given by sub-tree roots.
     * @param ancestorUpdated if an ancestor area has been updated
     * @return the index after processing the list of areas.
     */
    private int recomputeTreeSortOrder(List<Area> areas, int index, List<Area> updatedAreas, boolean ancestorUpdated) {

        for (Area area : areas) {
            index++;
            boolean updated = ancestorUpdated;
            if (index != area.getTreeSortOrder()) {
                area.setTreeSortOrder(index);
                updated = true;
                if (!ancestorUpdated) {
                    updatedAreas.add(area);
                }
            }

            // NB: area.getChildren() is by definition sorted
            index = recomputeTreeSortOrder(area.getChildren(), index, updatedAreas, updated);
        }

        return index;
    }

}
