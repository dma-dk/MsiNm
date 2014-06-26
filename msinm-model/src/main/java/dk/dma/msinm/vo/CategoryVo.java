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
    Integer parentId;
    List<CategoryVo> children = new ArrayList<>();

    /**
     * Constructor
     */
    public CategoryVo() {
    }

    /**
     * Constructor
     * @param category the category
     * @param includeChildren whether to include child categories or not
     */
    public CategoryVo(Category category, boolean includeChildren) {
        super(category);

        id = category.getId();
        category.getDescs().forEach(desc -> getDescs().add(new CategoryDescVo(desc)));
        if (includeChildren) {
            category.getChildren().forEach(child -> children.add(new CategoryVo(child)));
        }
    }

    /**
     * Constructor
     * @param category the category
     */
    public CategoryVo(Category category) {
        this(category, true);
    }

    /**
     * Constructor
     *
     * This version only reads the description records with given language,
     * and discards locations and child categories
     *
     * @param category the category
     */
    public CategoryVo(Category category, String lang) {
        super(category);

        id = category.getId();
        parentId = (category.getParent() == null) ? null : category.getParent().getId();
        category.getDescs().stream()
                .filter(desc -> desc.getLang().equals(lang))
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


    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
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
        CategoryDescVo category = new CategoryDescVo();
        category.setLang(lang);
        return category;
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
            desc.setName(name);
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
