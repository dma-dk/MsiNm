package dk.dma.msinm.templates.service;

import dk.dma.msinm.templates.vo.CompositeParamTypeVo;
import dk.dma.msinm.templates.vo.ListParamTypeVo;
import dk.dma.msinm.templates.vo.TemplateVo;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Lock;
import javax.ejb.LockType;
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
import java.util.List;

/**
 * REST interface to access .
 */
@Path("/templates")
@SecurityDomain("msinm-policy")
@PermitAll
public class TemplateRestService {

    @Inject
    TemplateService templateService;

    /*****************************************/
    /** Template methods                    **/
    /*****************************************/

    @GET
    @Path("/all")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    @Lock(LockType.READ)
    public List<TemplateVo> getTemplates(@QueryParam("lang") String lang) {
        return templateService.getTemplates(lang);
    }

    @POST
    @Path("/template")
    @Consumes("application/json")
    @Produces("application/json")
    @RolesAllowed({"admin"})
    public String createTemplate(TemplateVo templateVo) throws Exception {
        templateService.createTemplate(templateVo);
        return "OK";
    }

    @PUT
    @Path("/template")
    @Consumes("application/json")
    @Produces("application/json")
    @RolesAllowed({"admin"})
    public String updateTemplate(TemplateVo templateVo) throws Exception {
        templateService.updateTemplate(templateVo);
        return "OK";
    }

    @DELETE
    @Path("/template/{templateId}")
    @Produces("application/json")
    @RolesAllowed({"admin"})
    public String deleteTemplate(@PathParam("templateId") Integer templateId) throws Exception {
        templateService.deleteTemplate(templateId);
        return "OK";
    }

    /*****************************************/
    /** Common parameter type methods       **/
    /*****************************************/

    @GET
    @Path("/param-type-names")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    @Lock(LockType.READ)
    public List<String> getParameterTypeNames() {
        return templateService.getParameterTypeNames();
    }

    /*****************************************/
    /** List parameter type methods         **/
    /*****************************************/

    @GET
    @Path("/list-param-types")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    @Lock(LockType.READ)
    public List<ListParamTypeVo> getListParameterTypes(@QueryParam("lang") String lang) {
        return templateService.getListParamTypes(lang);
    }

    @POST
    @Path("/list-param-type")
    @Consumes("application/json")
    @Produces("application/json")
    @RolesAllowed({"admin"})
    public String createListParameterType(ListParamTypeVo typeVo) throws Exception {
        templateService.createListParamType(typeVo);
        return "OK";
    }

    @PUT
    @Path("/list-param-type")
    @Consumes("application/json")
    @Produces("application/json")
    @RolesAllowed({"admin"})
    public String updateListParameterType(ListParamTypeVo typeVo) throws Exception {
        templateService.updateListParamType(typeVo);
        return "OK";
    }

    @DELETE
    @Path("/list-param-type/{typeId}")
    @Produces("application/json")
    @RolesAllowed({"admin"})
    public String deleteListParameterType(@PathParam("typeId") Integer typeId) throws Exception {
        templateService.deleteParamType(typeId);
        return "OK";
    }

    /*****************************************/
    /** List parameter type methods         **/
    /*****************************************/

    @GET
    @Path("/composite-param-types")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    @Lock(LockType.READ)
    public List<CompositeParamTypeVo> getCompositeParameterTypes() {
        return templateService.getCompositeParamTypes();
    }

    @POST
    @Path("/composite-param-type")
    @Consumes("application/json")
    @Produces("application/json")
    @RolesAllowed({"admin"})
    public String createCompositeParameterType(CompositeParamTypeVo typeVo) throws Exception {
        templateService.createCompositeParamType(typeVo);
        return "OK";
    }

    @PUT
    @Path("/composite-param-type")
    @Consumes("application/json")
    @Produces("application/json")
    @RolesAllowed({"admin"})
    public String updateCompositeParameterType(CompositeParamTypeVo typeVo) throws Exception {
        templateService.updateCompositeParamType(typeVo);
        return "OK";
    }

    @DELETE
    @Path("/composite-param-type/{typeId}")
    @Produces("application/json")
    @RolesAllowed({"admin"})
    public String deleteCompositeParameterType(@PathParam("typeId") Integer typeId) throws Exception {
        templateService.deleteParamType(typeId);
        return "OK";
    }


}
