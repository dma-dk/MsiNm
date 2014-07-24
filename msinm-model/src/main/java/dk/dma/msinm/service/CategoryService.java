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
package dk.dma.msinm.service;

import dk.dma.msinm.common.MsiNmApp;
import dk.dma.msinm.common.db.PredicateHelper;
import dk.dma.msinm.common.db.Sql;
import dk.dma.msinm.common.model.DataFilter;
import dk.dma.msinm.common.service.BaseService;
import dk.dma.msinm.model.Category;
import dk.dma.msinm.model.CategoryDesc;
import dk.dma.msinm.vo.CategoryVo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.*;
import java.util.ArrayList;
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
    private MessageService messageService;

    @Inject
    private MsiNmApp app;

    @Inject
    @Sql("/sql/category_messages.sql")
    private String categoryMessagesSql;

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
        Map<Integer, CategoryVo> categoryLookup = new HashMap<>();
        categories.stream()
                .forEach(category -> categoryLookup.put(category.getId(), new CategoryVo(category, DataFilter.get(DataFilter.PARENT_ID).setLang(language))));


        // Add non-roots as child categories to their parent category
        categoryLookup.values().stream()
                .filter(categoryVo -> categoryVo.getParent() != null)
                .forEach(categoryVo -> categoryLookup.get(categoryVo.getParent().getId()).checkCreateChildren().add(categoryVo));

        // Return roots
        return categoryLookup.values().stream()
                .filter(categoryVo -> categoryVo.getParent() == null)
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
        return new CategoryVo(category, DataFilter.get());
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

        // Update lineage
        original.updateLineage();

        original = saveEntity(original);

        // Evict all cached messages for the category subtree
        evictCachedMessages(original);

        return original;
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

        category = saveEntity(category);

        // The category now has an ID - Update lineage
        category.updateLineage();
        category = saveEntity(category);

        em.flush();
        return category;
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

        category = saveEntity(category);
        em.flush();

        // Update all lineages
        updateLineages();

        // Return the update area
        category = getByPrimaryKey(Category.class, category.getId());

        // Evict all cached messages for the category subtree
        evictCachedMessages(category);

        return category;
    }

    /**
     * Evict all cached messages for the given subtree of areas
     * @param category the subtree to evict cached messaged for
     */
    private void evictCachedMessages(Category category) {
        // Sanity check
        if (category == null || category.getLineage() == null) {
            return;
        }

        String sql = categoryMessagesSql.replace(":lineage", "'" + category.getLineage() + "%'");

        List<?> ids = em.createNativeQuery(sql)
                .getResultList();

        ids.forEach(o -> messageService.evictCachedMessageId(((Number) o).intValue()));
    }

    /**
     * Update lineages for all categories
     */
    public void updateLineages() {

        log.info("Update category lineages");

        // Get root areas
        List<Category> roots = getAll(Category.class).stream()
                .filter(Category::isRootCategory)
                .collect(Collectors.toList());

        // Update each root subtree
        List<Category> updated = new ArrayList<>();
        roots.forEach(category -> updateLineages(category, updated));

        // Persist the changes
        updated.forEach(this::saveEntity);
        em.flush();
    }

    /**
     * Recursively updates the lineages of categories rooted at the given category
     * @param category the category whose sub-tree should be updated
     * @param categories the list of updated categories
     * @return if the lineage was updated
     */
    private boolean updateLineages(Category category, List<Category> categories) {

        boolean updated = category.updateLineage();
        if (updated) {
            categories.add(category);
        }
        category.getChildren().forEach(childCategory -> updateLineages(childCategory, categories));
        return updated;
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

    /**
     * Looks up an category by name
     * @param name the name to search for
     * @param lang the language. Optional
     * @param parentId the parent ID. Optional
     * @return The matching category, or null if not found
     */
    public Category findByName(String name, String lang, Integer parentId) {
        // Sanity check
        if (StringUtils.isBlank(name)) {
            return null;
        }

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Category> categoryQuery = builder.createQuery(Category.class);

        Root<Category> categoryRoot = categoryQuery.from(Category.class);

        // Build the predicate
        PredicateHelper<Category> predicateBuilder = new PredicateHelper<>(builder, categoryQuery);

        // Match the name
        Join<Category, CategoryDesc> descs = categoryRoot.join("descs", JoinType.LEFT);
        predicateBuilder.like(descs.get("name"), name);
        // Optionally, match the language
        if (StringUtils.isNotBlank(lang)) {
            predicateBuilder.equals(descs.get("lang"), lang);
        }

        // Optionally, match the parent
        if (parentId != null) {
            categoryRoot.join("parent", JoinType.LEFT);
            Path<Category> parent = categoryRoot.get("parent");
            predicateBuilder.equals(parent.get("id"), parentId);
        }

        // Complete the query
        categoryQuery.select(categoryRoot)
                .distinct(true)
                .where(predicateBuilder.where());

        // Execute the query and update the search result
        List<Category> result = em
                .createQuery(categoryQuery)
                .getResultList();

        return result.size() > 0 ? result.get(0) : null;
    }


    /**
     * Ensures that the template category and it's parents exists
     * @param templateCategory the template category
     * @return the category
     */
    public Category findOrCreateCategory(Category templateCategory) {
        // Sanity checks
        if (templateCategory == null || templateCategory.getDescs().size() == 0) {
            return null;
        }

        // Recursively, resolve the parent categories
        Category parent = null;
        if (templateCategory.getParent() != null) {
            parent = findOrCreateCategory(templateCategory.getParent());
        }
        Integer parentId = (parent == null) ? null : parent.getId();

        // Check if we can find the given category
        Category category = null;
        for (int x = 0; category == null && x < templateCategory.getDescs().size(); x++) {
            CategoryDesc desc = templateCategory.getDescs().get(x);
            category = findByName(desc.getName(), desc.getLang(), parentId);
        }

        // Create the category if no matching category was found
        if (category == null) {
            category = createCategory(templateCategory, parentId);
        }
        return category;
    }

}
