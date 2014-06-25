package dk.dma.msinm.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import dk.dma.msinm.model.Area;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Value object for the {@code Area} model entity
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class AreaVo implements Serializable {
    Integer id;
    Integer parentId;
    String nameLocal, nameEnglish;
    List<LocationVo> locations = new ArrayList<>();
    List<AreaVo> childAreas = new ArrayList<>();

    /**
     * Constructor
     */
    public AreaVo() {
    }

    /**
     * Constructor
     * @param area the area
     */
    public AreaVo(Area area) {
        id = area.getId();
        nameLocal = area.getOrCreateDesc("da").getName();
        nameEnglish = area.getOrCreateDesc("en").getName();
        area.getLocations().forEach(loc -> locations.add(new LocationVo(loc)));
        area.getChildAreas().forEach(childArea -> childAreas.add(new AreaVo(childArea)));
    }

    /**
     * Converts this area to a an Area
     * @return the area
     */
    public Area toArea() {
        Area area = new Area();
        area.setId(id);
        area.getOrCreateDesc("da").setName(nameLocal);
        area.getOrCreateDesc("en").setName(nameEnglish);
        locations.forEach(loc -> area.getLocations().add(loc.toLocation()));
        return area;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getNameLocal() {
        return nameLocal;
    }

    public void setNameLocal(String nameLocal) {
        this.nameLocal = nameLocal;
    }

    public String getNameEnglish() {
        return nameEnglish;
    }

    public void setNameEnglish(String nameEnglish) {
        this.nameEnglish = nameEnglish;
    }

    public List<LocationVo> getLocations() {
        return locations;
    }

    @JsonDeserialize(contentAs = LocationVo.class)
    public void setLocations(List<LocationVo> locations) {
        this.locations = locations;
    }

    public List<AreaVo> getChildAreas() {
        return childAreas;
    }

    public void setChildAreas(List<AreaVo> childAreas) {
        this.childAreas = childAreas;
    }

}
