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
            area.setParent(parent);
            parent.getChildren().add(area);
        }

        return saveEntity(area);
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
