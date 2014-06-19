package dk.dma.msinm.service;

import dk.dma.msinm.common.service.BaseService;
import dk.dma.msinm.model.Area;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.slf4j.Logger;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

/**
 * Business interface for accessing MSI-NM messages
 */
@Stateless
@SecurityDomain("msinm-policy")
@PermitAll
public class AreaService extends BaseService {

    @Inject
    private Logger log;

    /**
     * Returns the hierarchical list of root areas
     * @return the hierarchical list of root areas
     */
    public List<Area> getAreas() {
        return em.createNamedQuery("Area.findRootAreas", Area.class)
                .getResultList();
    }

    /**
     * Updates the area data from the area template, but not the parent-child hierarchy of the area
     * @param area the area to update
     * @return the updated area
     */
    public Area updateAreaData(Area area) {
        Area original = getByPrimaryKey(Area.class, area.getId());

        // Copy the area data
        original.setNameEnglish(area.getNameEnglish());
        original.setNameLocal(area.getNameLocal());
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


}
