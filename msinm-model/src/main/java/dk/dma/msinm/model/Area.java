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
public class Area extends VersionedEntity<Integer> implements ILocalizable<AreaDesc>, IPreloadable, Comparable<Area> {

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH })
    private Area parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @OrderBy("sortOrder ASC")
    private List<Area> children = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    private List<Location> locations = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "entity", orphanRemoval = true)
    List<AreaDesc> descs = new ArrayList<>();

    @Column(length = 256)
    String lineage;

    // The sortOrder is used to sort this area among siblings, and exposed via the Admin UI
    @Column(columnDefinition="DOUBLE default 0.0")
    double sortOrder;

    // The treeSortOrder is re-computed at regular intervals by the system and designates
    // the index of the area in an entire sorted area tree. Used for area sorting.
    @Column(columnDefinition="INT default 0")
    int treeSortOrder;

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
        // Add the area to the end of the children list
        Area lastChild = children.isEmpty() ? null : children.get(children.size() - 1);
        area.setSortOrder(lastChild == null ? Math.random() : lastChild.getSortOrder() + 10.0d);

        // Give it initial tree sort order. Won't really be correct until the tree sort order has
        // been re-computed for the entire tree.
        area.setTreeSortOrder(lastChild == null ? treeSortOrder : lastChild.getTreeSortOrder());

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
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Area area) {
        return (area == null || sortOrder == area.getSortOrder()) ? 0 : (sortOrder < area.getSortOrder() ? -1 : 1);
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

    public double getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(double sortOrder) {
        this.sortOrder = sortOrder;
    }

    public int getTreeSortOrder() {
        return treeSortOrder;
    }

    public void setTreeSortOrder(int treeSortOrder) {
        this.treeSortOrder = treeSortOrder;
    }
}

