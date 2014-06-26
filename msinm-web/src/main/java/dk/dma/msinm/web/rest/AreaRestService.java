package dk.dma.msinm.web.rest;

import dk.dma.msinm.model.Area;
import dk.dma.msinm.service.AreaService;
import dk.dma.msinm.vo.AreaVo;
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
 * REST interface for accessing MSI-NM areas
 */
@Path("/admin/areas")
@Stateless
@SecurityDomain("msinm-policy")
@PermitAll
public class AreaRestService {

    @Inject
    Logger log;

    @Inject
    AreaService areaService;


    /**
     * Returns all areas via a list of hierarchical root areas
     * @return returns all areas
     */
    @GET
    @Path("/area-roots")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    public List<AreaVo> getAreaRoots(@QueryParam("lang") String lang) {
        return areaService.getAreaTreeForLanguage(lang);
    }

    @GET
    @Path("/area/{areaId}")
    @Produces("application/json")
    public AreaVo getArea(@PathParam("areaId") Integer areaId) throws Exception {
        log.info("Getting area " + areaId);
        return areaService.getAreaDetails(areaId);
    }

    @POST
    @Path("/area")
    @Consumes("application/json")
    @Produces("application/json")
    @RolesAllowed({ "admin" })
    public String createArea(AreaVo areaVo) throws Exception {
        Area area = areaVo.toEntity();
        log.info("Creating area " + area);
        areaService.createArea(area, areaVo.getParentId());
        return "OK";
    }

    @PUT
    @Path("/area")
    @Consumes("application/json")
    @Produces("application/json")
    @RolesAllowed({ "admin" })
    public String updateArea(AreaVo areaVo) throws Exception {
        Area area = areaVo.toEntity();
        log.info("Updating area " + area);
        areaService.updateAreaData(area);
        return "OK";
    }

    @DELETE
    @Path("/area/{areaId}")
    @Produces("application/json")
    @RolesAllowed({ "admin" })
    public String deleteArea(@PathParam("areaId") Integer areaId) throws Exception {
        log.info("Deleting area " + areaId);
        areaService.deleteArea(areaId);
        return "OK";
    }

    @PUT
    @Path("/move-area")
    @Consumes("application/json")
    @Produces("application/json")
    @RolesAllowed({ "admin" })
    public String moveArea(MoveAreaVo moveAreaVo) throws Exception {
        log.info("Moving area " + moveAreaVo.getAreaId() + " to " + moveAreaVo.getParentId());
        areaService.moveArea(moveAreaVo.getAreaId(), moveAreaVo.getParentId());
        return "OK";
    }


    /*********************
     * Helper classes
     *********************/

    public static class MoveAreaVo implements Serializable {
        Integer areaId, parentId;

        public Integer getAreaId() {
            return areaId;
        }

        public void setAreaId(Integer areaId) {
            this.areaId = areaId;
        }

        public Integer getParentId() {
            return parentId;
        }

        public void setParentId(Integer parentId) {
            this.parentId = parentId;
        }
    }


}
