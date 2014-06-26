package dk.dma.msinm.model;

import dk.dma.msinm.common.model.ILocalizable;
import dk.dma.msinm.common.model.VersionedEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a specific named category, part of an category-hierarchy
 */
@Entity
@NamedQueries({
        @NamedQuery(name  = "Category.findRootCategories",
                query = "select distinct c from Category c left join fetch c.childCategories where c.parentCategory is null"),
        @NamedQuery(name  = "Category.findCategoriesWithDescs",
                query = "select distinct c from Category c left join fetch c.descs")
})
public class Category extends VersionedEntity<Integer> implements ILocalizable<CategoryDesc> {

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private Category parentCategory;

    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL)
    private List<Category> childCategories = new ArrayList<>();

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
    public void addChildCategory(Category category) {
        childCategories.add(category);
        category.setParentCategory(this);
    }

    /**
     * Checks if this is a root category
     *
     * @return if this is a root category
     */
    @Transient
    public boolean isRootCategory() {
        return parentCategory == null;
    }

    public Category getParentCategory() {
        return parentCategory;
    }

    public void setParentCategory(Category parentCategory) {
        this.parentCategory = parentCategory;
    }

    public List<Category> getChildCategories() {
        return childCategories;
    }

    public void setChildCategories(List<Category> childCategories) {
        this.childCategories = childCategories;
    }
}
