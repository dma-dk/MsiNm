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
package dk.dma.msinm.model;

import dk.dma.msinm.common.model.DataFilter;
import dk.dma.msinm.common.model.ILocalizable;
import dk.dma.msinm.common.model.IPreloadable;
import dk.dma.msinm.common.model.VersionedEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a specific named area, part of an area-hierarchy
 */
@Entity
@NamedQueries({
        @NamedQuery(name  = "Area.searchAreas",
                query = "select distinct a from Area a left join a.descs d where d.lang = :lang and lower(d.name) like lower(:term) "
                      + "order by LOCATE(lower(:sort), lower(d.name))"),
        @NamedQuery(name  = "Area.findRootAreas",
                query = "select distinct a from Area a left join fetch a.children where a.parent is null"),
        @NamedQuery(name  = "Area.findAreasWithDescs",
                query = "select distinct a from Area a left join fetch a.descs")
})
public class Area extends VersionedEntity<Integer> implements ILocalizable<AreaDesc>, IPreloadable {

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private Area parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Area> children = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    private List<Location> locations = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "entity")
    List<AreaDesc> descs = new ArrayList<>();

    @Column(length = 256)
    String lineage;

    @Override
    public List<AreaDesc> getDescs() {
        return descs;
    }

    @Override
    public void setDescs(List<AreaDesc> descs) {
        this.descs = descs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AreaDesc createDesc(String lang) {
        AreaDesc desc = new AreaDesc();
        desc.setLang(lang);
        desc.setEntity(this);
        getDescs().add(desc);
        return desc;
    }

    /**
     * Adds a child area, and ensures that all references are properly updated
     *
     * @param area the area to add
     */
    public void addChild(Area area) {
        children.add(area);
        area.setParent(this);
    }

    /**
     * Update the lineage to have the format "/root-id/.../parent-id/id"
     * @return if the lineage was updated
     */
    public boolean updateLineage() {
        String oldLineage = lineage;
        lineage = getParent() == null
                ? "/" + id + "/"
                : getParent().getLineage() + id + "/";
        return !lineage.equals(oldLineage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preload(DataFilter dataFilter) {
        DataFilter compFilter = dataFilter.forComponent(Area.class);

        if (compFilter.includeParent() && getParent() != null) {
            getParent().preload(compFilter);
        }
        if (compFilter.includeChildren()) {
            getChildren().forEach(child -> child.preload(compFilter));
        }
        if (compFilter.includeLocations()) {
            getLocations().forEach(loc -> loc.preload(compFilter));
        }
        getDescs().forEach(desc -> {});
    }

    /**
     * Checks if this is a root area
     *
     * @return if this is a root area
     */
    @Transient
    public boolean isRootArea() {
        return parent == null;
    }

    public Area getParent() {
        return parent;
    }

    public void setParent(Area parent) {
        this.parent = parent;
    }

    public List<Area> getChildren() {
        return children;
    }

    public void setChildren(List<Area> children) {
        this.children = children;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    public String getLineage() {
        return lineage;
    }

    public void setLineage(String lineage) {
        this.lineage = lineage;
    }
}

