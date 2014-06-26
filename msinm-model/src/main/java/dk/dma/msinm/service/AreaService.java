package dk.dma.msinm.service;

import dk.dma.msinm.common.MsiNmApp;
import dk.dma.msinm.common.service.BaseService;
import dk.dma.msinm.model.Area;
import dk.dma.msinm.vo.AreaVo;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
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
                .forEach(area -> areaLookup.put(area.getId(), new AreaVo(area, language)));


        // Add non-roots as child areas to their parent area
        areaLookup.values().stream()
                .filter(areaVo -> areaVo.getParentId() != null)
                .forEach(areaVo -> areaLookup.get(areaVo.getParentId()).getChildAreas().add(areaVo));

        // Return roots
        return areaLookup.values().stream()
                .filter(areaVo -> areaVo.getParentId() == null)
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
        return new AreaVo(area, false);
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
    public Area createArea(Area area, Integer parentId) {

        if (parentId != null) {
            Area parent = getByPrimaryKey(Area.class, parentId);
            parent.getChildAreas().add(area);
            area.setParentArea(parent);
        }

        return saveEntity(area);
    }

    /**
     * Moves the area to the given parent id
     * @param areaId the id of the area to create
     * @param parentId the id of the parent area
     * @return the updated area
     */
    public Area moveArea(Integer areaId, Integer parentId) {
        Area area = getByPrimaryKey(Area.class, areaId);

        if (area.getParentArea() != null && !area.getParentArea().getId().equals(parentId)) {
            area.getParentArea().getChildAreas().remove(area);
        }

        if (parentId == null) {
            area.setParentArea(null);
        } else {
            Area parent = getByPrimaryKey(Area.class, parentId);
            area.setParentArea(parent);
            parent.getChildAreas().add(area);
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
            area.setParentArea(null);
            saveEntity(area);
            remove(area);
            return true;
        }
        return false;
    }


}
