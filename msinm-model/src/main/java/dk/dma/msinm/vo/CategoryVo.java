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
package dk.dma.msinm.vo;

import dk.dma.msinm.common.model.DataFilter;
import dk.dma.msinm.common.vo.LocalizableVo;
import dk.dma.msinm.common.vo.LocalizedDescVo;
import dk.dma.msinm.model.Category;
import dk.dma.msinm.model.CategoryDesc;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Value object for the {@code Category} model entity
 */
public class CategoryVo extends LocalizableVo<Category, CategoryVo.CategoryDescVo> {
    Integer id;
    CategoryVo parent;
    List<CategoryVo> children;

    /**
     * Constructor
     */
    public CategoryVo() {
    }

    /**
     * Constructor
     *
     * @param category the category
     * @param dataFilter what type of data to include from the entity
     */
    public CategoryVo(Category category, DataFilter dataFilter) {
        super(category);

        DataFilter compFilter = dataFilter.forComponent(Category.class);

        id = category.getId();

        if (compFilter.includeChildren()) {
            category.getChildren().forEach(child -> checkCreateChildren().add(new CategoryVo(child, compFilter)));
        }

        if (compFilter.includeParent() && category.getParent() != null) {
            parent = new CategoryVo(category.getParent(), compFilter);
        } else if (compFilter.includeParentId() && category.getParent() != null) {
            parent = new CategoryVo();
            parent.setId(category.getParent().getId());
        }

        category.getDescs().stream()
            .filter(compFilter::includeLang)
            .forEach(desc -> checkCreateDescs().add(new CategoryDescVo(desc)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Category toEntity() {
        Category category = new Category();
        category.setId(id);
        if (getDescs() != null) {
            getDescs().stream()
                    .filter(desc -> StringUtils.isNotBlank(desc.getName()))
                    .forEach(desc -> category.getDescs().add(desc.toEntity(category)));
        }
        return category;
    }

    /**
     * Returns or creates the list of child categories
     * @return the list of child categories
     */
    public List<CategoryVo> checkCreateChildren() {
        if (children == null) {
            children = new ArrayList<>();
        }
        return children;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public CategoryVo getParent() {
        return parent;
    }

    public void setParent(CategoryVo parent) {
        this.parent = parent;
    }

    public List<CategoryVo> getChildren() {
        return children;
    }

    public void setChildren(List<CategoryVo> children) {
        this.children = children;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CategoryDescVo createDesc(String lang) {
        CategoryDescVo desc = new CategoryDescVo();
        checkCreateDescs().add(desc);
        desc.setLang(lang);
        return desc;
    }

    /**
     * The entity description VO
     */
    public static class CategoryDescVo extends LocalizedDescVo<CategoryDesc, CategoryVo> {

        String name;

        /**
         * Constructor
         */
        public CategoryDescVo() {
            super();
        }

        /**
         * Constructor
         * @param desc the entity
         */
        public CategoryDescVo(CategoryDesc desc) {
            super(desc);
            name = desc.getName();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public CategoryDesc toEntity() {
            CategoryDesc desc = new CategoryDesc();
            desc.setLang(getLang());
            desc.setName(StringUtils.trim(name));
            return desc;
        }

        public CategoryDesc toEntity(Category category) {
            CategoryDesc desc = toEntity();
            desc.setEntity(category);
            return desc;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
