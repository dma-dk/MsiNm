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

import dk.dma.msinm.model.Chart;
import dk.dma.msinm.model.Location;
import dk.dma.msinm.service.ChartService;
import dk.dma.msinm.vo.ChartVo;
import dk.dma.msinm.vo.LocationVo;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST interface for accessing MSI-NM charts
 */
@Path("/admin/charts")
@Stateless
@SecurityDomain("msinm-policy")
@PermitAll
public class ChartRestService {

    @Inject
    Logger log;

    @Inject
    ChartService chartService;


    /***************************
     ** Charts
     ***************************/

    /**
     * Searchs for charts matching the given term
     * @param term the search term
     * @param limit the maximum number of results
     * @return the search result
     */
    @GET
    @Path("/search")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    public List<ChartVo> searchCharts(@QueryParam("term") String term, @QueryParam("limit") int limit) {
        log.info(String.format("Searching for charts term='%s', limit=%d", term, limit));
        return chartService.searchCharts(term, limit);
    }

    /**
     * Returns all charts
     * @return returns all charts
     */
    @GET
    @Path("/all")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    public List<Chart> getCharts() {
        return chartService.getCharts();
    }

    @POST
    @Path("/chart")
    @Consumes("application/json")
    @Produces("application/json")
    @RolesAllowed({ "admin" })
    public String createChart(ChartVo chartVo) throws Exception {
        Chart chart = chartVo.toEntity();
        log.info("Updating chart " + chart);
        chartService.createChart(chart);
        return "OK";
    }

    @PUT
    @Path("/chart")
    @Consumes("application/json")
    @Produces("application/json")
    @RolesAllowed({ "admin" })
    public String updateChart(ChartVo chartVo) throws Exception {
        Chart chart = chartVo.toEntity();
        log.info("Updating chart " + chart);
        chartService.updateChartData(chart);
        return "OK";
    }

    @DELETE
    @Path("/chart/{chartId}")
    @Produces("application/json")
    @RolesAllowed({ "admin" })
    public String deleteChart(@PathParam("chartId") Integer chartId) throws Exception {
        log.info("Deleting chart " + chartId);
        chartService.deleteChart(chartId);
        return "OK";
    }

    /**
     * Computes the list of charts whose bounds intersects with
     * the given location list.<br>
     * By <i>intersects</i> we include all relationships such as
     * "within", "contains" and "intersects" in the strict sense.
     *
     * @return if the locations intersects this chart
     */
    @POST
    @Path("/intersecting-charts")
    @Consumes("application/json")
    @Produces("application/json;charset=UTF-8")
    @RolesAllowed({ "editor" })
    @GZIP
    @NoCache
    public List<ChartVo> getIntersectingCharts(List<LocationVo> locationVos) {

        List<Location> locations = locationVos.stream()
                .map(LocationVo::toEntity)
                .collect(Collectors.toList());

        return chartService.getIntersectingCharts(locations).stream()
                .map(ChartVo::new)
                .collect(Collectors.toList());
    }
}
