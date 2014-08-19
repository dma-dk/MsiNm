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
package dk.dma.msinm.web.rest;

import dk.dma.msinm.model.Category;
import dk.dma.msinm.service.CategoryService;
import dk.dma.msinm.vo.CategoryVo;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.io.Serializable;
import java.util.List;


/**
 * REST interface for accessing MSI-NM categories
 */
@Path("/admin/categories")
@Stateless
@SecurityDomain("msinm-policy")
@PermitAll
public class CategoryRestService {

    @Inject
    Logger log;

    @Inject
    CategoryService categoryService;


    /**
     * Searchs for categories matching the given term in the given language
     * @param lang the language
     * @param term the search term
     * @param limit the maximum number of results
     * @return the search result
     */
    @GET
    @Path("/search")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    public List<CategoryVo> searchCategories(@QueryParam("lang") String lang, @QueryParam("term") String term, @QueryParam("limit") int limit) {
        log.info(String.format("Searching for categories lang=%s, term='%s', limit=%d", lang, term, limit));
        return categoryService.searchCategories(lang, term, limit);
    }

    /**
     * Returns all categories via a list of hierarchical root categories
     * @return returns all categories
     */
    @GET
    @Path("/category-roots")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    public List<CategoryVo> getCategoryRoots(@QueryParam("lang") String lang) {
        return categoryService.getCategoryTreeForLanguage(lang);
    }

    @GET
    @Path("/category/{categoryId}")
    @Produces("application/json")
    public CategoryVo getCategory(@PathParam("categoryId") Integer categoryId) throws Exception {
        log.info("Getting category " + categoryId);
        return categoryService.getCategoryDetails(categoryId);
    }

    @POST
    @Path("/category")
    @Consumes("application/json")
    @Produces("application/json")
    @RolesAllowed({ "admin" })
    public String createCategory(CategoryVo categoryVo) throws Exception {
        Category category = categoryVo.toEntity();
        log.info("Creating category " + category);
        Integer parentId = (categoryVo.getParent() == null) ? null : categoryVo.getParent().getId();
        categoryService.createCategory(category, parentId);
        return "OK";
    }

    @PUT
    @Path("/category")
    @Consumes("application/json")
    @Produces("application/json")
    @RolesAllowed({ "admin" })
    public String updateCategory(CategoryVo categoryVo) throws Exception {
        Category category = categoryVo.toEntity();
        log.info("Updating category " + category);
        categoryService.updateCategoryData(category);
        return "OK";
    }

    @DELETE
    @Path("/category/{categoryId}")
    @Produces("application/json")
    @RolesAllowed({ "admin" })
    public String deleteCategory(@PathParam("categoryId") Integer categoryId) throws Exception {
        log.info("Deleting category " + categoryId);
        categoryService.deleteCategory(categoryId);
        return "OK";
    }

    @PUT
    @Path("/move-category")
    @Consumes("application/json")
    @Produces("application/json")
    @RolesAllowed({ "admin" })
    public String moveCategory(MoveCategoryVo moveCategoryVo) throws Exception {
        log.info("Moving category " + moveCategoryVo.getCategoryId() + " to " + moveCategoryVo.getParentId());
        categoryService.moveCategory(moveCategoryVo.getCategoryId(), moveCategoryVo.getParentId());
        return "OK";
    }


    /*********************
     * Helper classes
     *********************/

    public static class MoveCategoryVo implements Serializable {
        Integer categoryId, parentId;

        public Integer getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(Integer categoryId) {
            this.categoryId = categoryId;
        }

        public Integer getParentId() {
            return parentId;
        }

        public void setParentId(Integer parentId) {
            this.parentId = parentId;
        }
    }
}
