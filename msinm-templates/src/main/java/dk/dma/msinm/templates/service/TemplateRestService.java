package dk.dma.msinm.templates.service;

import dk.dma.msinm.templates.vo.ListParamTypeVo;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

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
    Logger log;

    @Inject
    ListParamService listParamService;


    /**
     * Returns all list parameter types.
     * NB: Returns all language variants, but sorted by the given language
     *
     * @return returns all list parameter types
     */
    @GET
    @Path("/allParameterTypes")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    @Lock(LockType.READ)
    public List<ListParamTypeVo> getListParameterTypes(@QueryParam("lang") String lang) {
        return listParamService.getListParamTypes(lang);
    }

    @POST
    @Path("/parameterType")
    @Consumes("application/json")
    @Produces("application/json")
    @RolesAllowed({"admin"})
    public String createListParameterType(ListParamTypeVo typeVo) throws Exception {
        listParamService.createParamType(typeVo);
        return "OK";
    }

    @PUT
    @Path("/parameterType")
    @Consumes("application/json")
    @Produces("application/json")
    @RolesAllowed({"admin"})
    public String updateListParameterType(ListParamTypeVo typeVo) throws Exception {
        listParamService.updateParamType(typeVo);
        return "OK";
    }

    @DELETE
    @Path("/parameterType/{typeId}")
    @Produces("application/json")
    @RolesAllowed({"admin"})
    public String deleteListParameterType(@PathParam("typeId") Integer typeId) throws Exception {
        listParamService.deleteParamType(typeId);
        return "OK";
    }


}
