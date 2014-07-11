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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown=true)
public class CategoryVo extends LocalizableVo<Category, CategoryVo.CategoryDescVo> {
    Integer id;
    CategoryVo parent;
    List<CategoryVo> children = new ArrayList<>();

    /**
     * Constructor
     */
    public CategoryVo() {
    }

    /**
     * Constructor
     *
     * @param category the category
     * @param copyOp what type of data to copy from the entity
     */
    public CategoryVo(Category category, CopyOp copyOp) {
        super(category);

        id = category.getId();

        if (copyOp.copy(CopyOp.CHILDREN)) {
            category.getChildren().forEach(child -> children.add(new CategoryVo(child, copyOp)));
        }

        if (copyOp.copy(CopyOp.PARENT) && category.getParent() != null) {
            parent = new CategoryVo(category.getParent(), copyOp);
        } else if (copyOp.copy(CopyOp.PARENT_ID) && category.getParent() != null) {
            parent = new CategoryVo();
            parent.setId(category.getParent().getId());
        }

        category.getDescs().stream()
            .filter(copyOp::copyLang)
            .forEach(desc -> getDescs().add(new CategoryDescVo(desc)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Category toEntity() {
        Category category = new Category();
        category.setId(id);
        getDescs().stream()
                .filter(desc -> StringUtils.isNotBlank(desc.getName()))
                .forEach(desc -> category.getDescs().add(desc.toEntity(category)));
        return category;
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
        getDescs().add(desc);
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
