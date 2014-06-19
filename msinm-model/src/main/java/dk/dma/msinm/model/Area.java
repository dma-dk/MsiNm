package dk.dma.msinm.model;

import dk.dma.msinm.common.model.VersionedEntity;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a specific named area, part of an area-hierarchy
 */
@Entity
@NamedQueries({
        @NamedQuery(name  = "Area.findRootAreas",
                    query = "select distinct a from Area a left join fetch a.childAreas where a.parentArea is null")
})
public class Area extends VersionedEntity<Integer> {

    @NotNull
    private String nameEnglish;

    @NotNull
    private String nameLocal;

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private Area parentArea;

    @OneToMany(mappedBy = "parentArea", cascade = CascadeType.ALL)
    private List<Area> childAreas = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    private List<MessageLocation> locations = new ArrayList<>();

    /**
     * Adds a child area, and ensures that all references are properly updated
     *
     * @param area the area to add
     */
    public void addChildArea(Area area) {
        childAreas.add(area);
        area.setParentArea(this);
    }

    /**
     * Checks if this is a root area
     *
     * @return if this is a root area
     */
    @Transient
    public boolean isRootArea() {
        return parentArea == null;
    }

    /**
     * Creates a Json representation of this entity
     * @return the Json representation
     */
    public JsonObjectBuilder toJson() {
        JsonArrayBuilder childAreasJson = Json.createArrayBuilder();
        childAreas.forEach(area -> childAreasJson.add(area.toJson()));
        JsonArrayBuilder locationsJson = Json.createArrayBuilder();
        locations.forEach(l -> locationsJson.add(l.toJson()));

        return Json.createObjectBuilder()
                .add("id", getId())
                .add("nameEnglish", getNameEnglish())
                .add("nameLocal", getNameLocal())
                .add("childAreas", childAreasJson)
                .add("locations", locationsJson);
    }

    public String getNameEnglish() {
        return nameEnglish;
    }

    public void setNameEnglish(String nameEnglish) {
        this.nameEnglish = nameEnglish;
    }

    public String getNameLocal() {
        return nameLocal;
    }

    public void setNameLocal(String nameLocal) {
        this.nameLocal = nameLocal;
    }

    public Area getParentArea() {
        return parentArea;
    }

    public void setParentArea(Area parentArea) {
        this.parentArea = parentArea;
    }

    public List<Area> getChildAreas() {
        return childAreas;
    }

    public void setChildAreas(List<Area> childAreas) {
        this.childAreas = childAreas;
    }

    public List<MessageLocation> getLocations() {
        return locations;
    }

    public void setLocations(List<MessageLocation> locations) {
        this.locations = locations;
    }

}

