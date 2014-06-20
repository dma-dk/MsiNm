package dk.dma.msinm.web.rest;

import dk.dma.msinm.model.Chart;
import dk.dma.msinm.service.ChartService;
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
import java.util.List;

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
    public String createChart(Chart chart) throws Exception {
        log.info("Updating chart " + chart);
        chartService.createChart(chart);
        return "OK";
    }

    @PUT
    @Path("/chart")
    @Consumes("application/json")
    @Produces("application/json")
    @RolesAllowed({ "admin" })
    public String updateChart(Chart chart) throws Exception {
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

}
