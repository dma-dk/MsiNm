package dk.dma.msinm.templates.service;

import dk.dma.msinm.common.vo.JsonSerializable;
import dk.dma.msinm.templates.vo.CompositeParamTypeVo;
import dk.dma.msinm.templates.vo.DictTermVo;
import dk.dma.msinm.templates.vo.FieldTemplateVo;
import dk.dma.msinm.templates.vo.FmIncludeVo;
import dk.dma.msinm.templates.vo.ListParamTypeVo;
import dk.dma.msinm.templates.vo.ParamTypeVo;
import dk.dma.msinm.templates.vo.ParameterDataVo;
import dk.dma.msinm.templates.vo.TemplateVo;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
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
    public List<TemplateVo> getTemplates(@QueryParam("lang") String lang) {
        return templateService.getTemplates(lang);
    }

    @POST
    @Path("/template")
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    @RolesAllowed({"admin"})
    public String createTemplate(TemplateVo templateVo) throws Exception {
        templateService.createTemplate(templateVo);
        return "OK";
    }

    @PUT
    @Path("/template")
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    @RolesAllowed({"admin"})
    public String updateTemplate(TemplateVo templateVo) throws Exception {
        templateService.updateTemplate(templateVo);
        return "OK";
    }

    @DELETE
    @Path("/template/{templateId}")
    @Produces("application/json;charset=UTF-8")
    @RolesAllowed({"admin"})
    public String deleteTemplate(@PathParam("templateId") Integer templateId) throws Exception {
        templateService.deleteTemplate(templateId);
        return "OK";
    }

    @GET
    @Path("/field-templates")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    public List<FieldTemplateVo> getFieldTemplates() {
        return templateService.getFieldTemplates();
    }

    /*****************************************/
    /** Common parameter type methods       **/
    /*****************************************/

    @GET
    @Path("/param-types")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    public List<ParamTypeVo> getParameterTypes(@QueryParam("lang") String lang) {
        return templateService.getParameterTypes(lang);
    }

    @GET
    @Path("/param-type-names")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
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
    public List<ListParamTypeVo> getListParameterTypes(@QueryParam("lang") String lang) {
        return templateService.getListParamTypes(lang);
    }

    @POST
    @Path("/list-param-type")
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    @RolesAllowed({"admin"})
    public String createListParameterType(ListParamTypeVo typeVo) throws Exception {
        templateService.createListParamType(typeVo);
        return "OK";
    }

    @PUT
    @Path("/list-param-type")
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    @RolesAllowed({"admin"})
    public String updateListParameterType(ListParamTypeVo typeVo) throws Exception {
        templateService.updateListParamType(typeVo);
        return "OK";
    }

    @DELETE
    @Path("/list-param-type/{typeId}")
    @Produces("application/json;charset=UTF-8")
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
    public List<CompositeParamTypeVo> getCompositeParameterTypes() {
        return templateService.getCompositeParamTypes();
    }

    @POST
    @Path("/composite-param-type")
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    @RolesAllowed({"admin"})
    public String createCompositeParameterType(CompositeParamTypeVo typeVo) throws Exception {
        templateService.createCompositeParamType(typeVo);
        return "OK";
    }

    @PUT
    @Path("/composite-param-type")
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    @RolesAllowed({"admin"})
    public String updateCompositeParameterType(CompositeParamTypeVo typeVo) throws Exception {
        templateService.updateCompositeParamType(typeVo);
        return "OK";
    }

    @DELETE
    @Path("/composite-param-type/{typeId}")
    @Produces("application/json;charset=UTF-8")
    @RolesAllowed({"admin"})
    public String deleteCompositeParameterType(@PathParam("typeId") Integer typeId) throws Exception {
        templateService.deleteParamType(typeId);
        return "OK";
    }


    // *******************************************
    // ** Dictionary functionality
    // *******************************************

    @GET
    @Path("/dict-terms")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    public List<DictTermVo> getDictTerms(@QueryParam("lang") String lang) {
        return templateService.getDictTerms(lang);
    }

    @POST
    @Path("/dict-term")
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    @RolesAllowed({"admin"})
    public String createDictTerm(DictTermVo dictTermVo) throws Exception {
        templateService.createDictTerm(dictTermVo);
        return "OK";
    }

    @PUT
    @Path("/dict-term")
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    @RolesAllowed({"admin"})
    public String updateDictTerm(DictTermVo dictTermVo) throws Exception {
        templateService.updateDictTerm(dictTermVo);
        return "OK";
    }

    @DELETE
    @Path("/dict-term/{dictTermId}")
    @Produces("application/json;charset=UTF-8")
    @RolesAllowed({"admin"})
    public String deleteDictTerm(@PathParam("dictTermId") Integer dictTermId) throws Exception {
        templateService.deleteDictTerm(dictTermId);
        return "OK";
    }


    // *******************************************
    // ** Freemarker functionality
    // *******************************************

    @GET
    @Path("/fm-includes")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    public List<FmIncludeVo> getFmIncludes() {
        return templateService.getFmIncludes();
    }

    @POST
    @Path("/fm-include")
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    @RolesAllowed({"admin"})
    public String createFmInclude(FmIncludeVo fmIncludeVo) throws Exception {
        templateService.createFmInclude(fmIncludeVo);
        return "OK";
    }

    @PUT
    @Path("/fm-include")
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    @RolesAllowed({"admin"})
    public String updateFmInclude(FmIncludeVo fmIncludeVo) throws Exception {
        templateService.updateFmInclude(fmIncludeVo);
        return "OK";
    }

    @DELETE
    @Path("/fm-include/{fmIncludeId}")
    @Produces("application/json;charset=UTF-8")
    @RolesAllowed({"admin"})
    public String deleteFmInclude(@PathParam("fmIncludeId") Integer fmIncludeId) throws Exception {
        templateService.deleteFmInclude(fmIncludeId);
        return "OK";
    }

    @POST
    @Path("/process-template")
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    @RolesAllowed({"editor"})
    public TemplateVo processTemplate(ProcessTemplateVo processTemplate) throws Exception {
        return templateService.processTemplate(
                processTemplate.getMsgId(),
                processTemplate.getTemplate(),
                processTemplate.getParams());
    }


    /***************************
     * Helper VO classes
     ***************************/

    /**
     * Helper class used for processing a template
     */
    public static class ProcessTemplateVo implements JsonSerializable {
        String msgId;
        TemplateVo template;
        List<ParameterDataVo> params;

        public String getMsgId() {
            return msgId;
        }

        public void setMsgId(String msgId) {
            this.msgId = msgId;
        }

        public TemplateVo getTemplate() {
            return template;
        }

        public void setTemplate(TemplateVo template) {
            this.template = template;
        }

        public List<ParameterDataVo> getParams() {
            return params;
        }

        public void setParams(List<ParameterDataVo> params) {
            this.params = params;
        }
    }
}
