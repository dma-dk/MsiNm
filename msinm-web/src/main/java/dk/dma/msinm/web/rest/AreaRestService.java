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
     * Searchs for areas matching the given term in the given language
     *
     * @param lang  the language
     * @param term  the search term
     * @param limit the maximum number of results
     * @return the search result
     */
    @GET
    @Path("/search")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    public List<AreaVo> searchAreas(@QueryParam("lang") String lang, @QueryParam("term") String term, @QueryParam("limit") int limit) {
        log.info(String.format("Searching for areas lang=%s, term='%s', limit=%d", lang, term, limit));
        return areaService.searchAreas(lang, term, limit);
    }

    /**
     * Returns all areas via a list of hierarchical root areas
     *
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
    @RolesAllowed({"admin"})
    public String createArea(AreaVo areaVo) throws Exception {
        Area area = areaVo.toEntity();
        log.info("Creating area " + area);
        Integer parentId = (areaVo.getParent() == null) ? null : areaVo.getParent().getId();
        areaService.createArea(area, parentId);
        return "OK";
    }

    @PUT
    @Path("/area")
    @Consumes("application/json")
    @Produces("application/json")
    @RolesAllowed({"admin"})
    public String updateArea(AreaVo areaVo) throws Exception {
        Area area = areaVo.toEntity();
        log.info("Updating area " + area);
        areaService.updateAreaData(area);
        return "OK";
    }

    @DELETE
    @Path("/area/{areaId}")
    @Produces("application/json")
    @RolesAllowed({"admin"})
    public String deleteArea(@PathParam("areaId") Integer areaId) throws Exception {
        log.info("Deleting area " + areaId);
        areaService.deleteArea(areaId);
        return "OK";
    }

    @PUT
    @Path("/move-area")
    @Consumes("application/json")
    @Produces("application/json")
    @RolesAllowed({"admin"})
    public String moveArea(MoveAreaVo moveAreaVo) throws Exception {
        log.info("Moving area " + moveAreaVo.getAreaId() + " to " + moveAreaVo.getParentId());
        areaService.moveArea(moveAreaVo.getAreaId(), moveAreaVo.getParentId());
        return "OK";
    }

    @PUT
    @Path("/change-sort-order")
    @Consumes("application/json")
    @Produces("application/json")
    @RolesAllowed({"admin"})
    public String changeSortOrder(ChangeSortOrderVo changeSortOrderVo) throws Exception {
        log.info("Changing sort-order of area " + changeSortOrderVo.getAreaId()
                + " moving " + (changeSortOrderVo.isMoveUp() ? "up" : "down"));
        areaService.changeSortOrder(changeSortOrderVo.getAreaId(), changeSortOrderVo.isMoveUp());
        return "OK";
    }


    /**
     * ******************
     * Helper classes
     * *******************
     */

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

    public static class ChangeSortOrderVo implements Serializable {
        Integer areaId;
        boolean moveUp;

        public Integer getAreaId() {
            return areaId;
        }

        public void setAreaId(Integer areaId) {
            this.areaId = areaId;
        }

        public boolean isMoveUp() {
            return moveUp;
        }

        public void setMoveUp(boolean moveUp) {
            this.moveUp = moveUp;
        }
    }
}
