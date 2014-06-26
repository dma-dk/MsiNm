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
import javax.ws.rs.*;
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
        categoryService.createCategory(category, categoryVo.getParentId());
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
