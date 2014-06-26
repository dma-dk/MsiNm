package dk.dma.msinm.service;

import dk.dma.msinm.common.MsiNmApp;
import dk.dma.msinm.common.service.BaseService;
import dk.dma.msinm.model.Category;
import dk.dma.msinm.vo.CategoryVo;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Business interface for accessing MSI-NM categories
 */
@Stateless
public class CategoryService extends BaseService {

    @Inject
    private Logger log;

    @Inject
    private MsiNmApp app;

    /**
     * Returns the hierarchical list of root categories.
     * <p></p>
     * The returned list is a condensed data set where only description records
     * for the given language is included and no locations are included.
     *
     * @param lang the language
     * @return the hierarchical list of root categories
     */
    public List<CategoryVo> getCategoryTreeForLanguage(String lang) {
        // Ensure validity
        final String language = app.getLanguage(lang);

        // Get all categories along with their CategoryDesc records
        // Will ensure that all Category entities are cached in the entity manager before organizing the result
        List<Category> categories = em
                .createNamedQuery("Category.findCategoriesWithDescs", Category.class)
                .getResultList();

        // Create a lookup map
        //Map<Integer, CategoryVo> categoryLookup = categories.stream()
        //        .collect(Collectors.toMap(Category::getId, category -> new CategoryVo(category, language)));
        Map<Integer, CategoryVo> categoryLookup = new HashMap<>();
        categories.stream()
                .forEach(category -> categoryLookup.put(category.getId(), new CategoryVo(category, language)));


        // Add non-roots as child categories to their parent category
        categoryLookup.values().stream()
                .filter(categoryVo -> categoryVo.getParentId() != null)
                .forEach(categoryVo -> categoryLookup.get(categoryVo.getParentId()).getChildren().add(categoryVo));

        // Return roots
        return categoryLookup.values().stream()
                .filter(categoryVo -> categoryVo.getParentId() == null)
                .collect(Collectors.toList());
    }

    /**
     * Looks up a category and the associated data, but does NOT look up
     * the child-category hierarchy
     *
     * @param id the id of the category
     * @return the category
     */
    public CategoryVo getCategoryDetails(Integer id) {
        Category category = getByPrimaryKey(Category.class, id);
        if (category == null) {
            return null;
        }

        // NB: No child categories included
        return new CategoryVo(category, false);
    }

    /**
     * Updates the category data from the category template, but not the parent-child hierarchy of the category
     * @param category the category to update
     * @return the updated category
     */
    public Category updateCategoryData(Category category) {
        Category original = getByPrimaryKey(Category.class, category.getId());

        // Copy the category data
        original.copyDescs(category.getDescs());

        return saveEntity(original);
    }

    /**
     * Creates a new category based on the category template
     * @param category the category to create
     * @param parentId the id of the parent category
     * @return the created category
     */
    public Category createCategory(Category category, Integer parentId) {

        if (parentId != null) {
            Category parent = getByPrimaryKey(Category.class, parentId);
            parent.getChildren().add(category);
            category.setParent(parent);
        }

        return saveEntity(category);
    }

    /**
     * Moves the category to the given parent id
     * @param categoryId the id of the category to create
     * @param parentId the id of the parent category
     * @return the updated category
     */
    public Category moveCategory(Integer categoryId, Integer parentId) {
        Category category = getByPrimaryKey(Category.class, categoryId);

        if (category.getParent() != null && !category.getParent().getId().equals(parentId)) {
            category.getParent().getChildren().remove(category);
        }

        if (parentId == null) {
            category.setParent(null);
        } else {
            Category parent = getByPrimaryKey(Category.class, parentId);
            category.setParent(parent);
            parent.getChildren().add(category);
        }

        return saveEntity(category);
    }

    /**
     * Deletes the category and sub-categories
     * @param categoryId the id of the category to delete
     */
    public boolean deleteCategory(Integer categoryId) {

        Category category = getByPrimaryKey(Category.class, categoryId);
        if (category != null) {
            category.setParent(null);
            saveEntity(category);
            remove(category);
            return true;
        }
        return false;
    }
}
