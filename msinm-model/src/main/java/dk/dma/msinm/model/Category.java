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
 * Represents a specific named category, part of an category-hierarchy
 */
@Entity
@NamedQueries({
        @NamedQuery(name  = "Category.searchCategories",
                query = "select distinct c from Category c left join c.descs d where d.lang = :lang and lower(d.name) like lower(:term) "
                        + "order by LOCATE(lower(:sort), lower(d.name))"),
        @NamedQuery(name  = "Category.findRootCategories",
                query = "select distinct c from Category c left join fetch c.children where c.parent is null"),
        @NamedQuery(name  = "Category.findCategoriesWithDescs",
                query = "select distinct c from Category c left join fetch c.descs")
})
public class Category extends VersionedEntity<Integer> implements ILocalizable<CategoryDesc>, IPreloadable {

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH })
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Category> children = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "entity")
    List<CategoryDesc> descs = new ArrayList<>();

    @Override
    public List<CategoryDesc> getDescs() {
        return descs;
    }

    @Override
    public void setDescs(List<CategoryDesc> descs) {
        this.descs = descs;
    }

    @Column(length = 256)
    String lineage;

    /**
     * {@inheritDoc}
     */
    @Override
    public CategoryDesc createDesc(String lang) {
        CategoryDesc desc = new CategoryDesc();
        desc.setLang(lang);
        desc.setEntity(this);
        getDescs().add(desc);
        return desc;
    }

    /**
     * Adds a child category, and ensures that all references are properly updated
     *
     * @param category the category to add
     */
    public void addChild(Category category) {
        children.add(category);
        category.setParent(this);
    }

    /**
     * Update the lineage to have the format "/root-id/.../parent-id/id"
     * @return if the lineage was updated
     */
    public boolean updateLineage() {
        String oldLineage = lineage;
        lineage = getParent() == null
                ? "/" + id  + "/"
                : getParent().getLineage() + id + "/";
        return !lineage.equals(oldLineage);
    }

    /**
     * Checks if this is a root category
     *
     * @return if this is a root category
     */
    @Transient
    public boolean isRootCategory() {
        return parent == null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preload(DataFilter dataFilter) {
        DataFilter compFilter = dataFilter.forComponent(Category.class);

        if (compFilter.includeParent() && getParent() != null) {
            getParent().preload(compFilter);
        }
        if (compFilter.includeChildren()) {
            getChildren().forEach(child -> child.preload(compFilter));
        }
        getDescs().forEach(desc -> {});
    }

    public Category getParent() {
        return parent;
    }

    public void setParent(Category parent) {
        this.parent = parent;
    }

    public List<Category> getChildren() {
        return children;
    }

    public void setChildren(List<Category> children) {
        this.children = children;
    }

    public String getLineage() {
        return lineage;
    }

    public void setLineage(String lineage) {
        this.lineage = lineage;
    }
}
