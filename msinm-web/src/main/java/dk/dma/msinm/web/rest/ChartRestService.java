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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dk.dma.msinm.model.Chart;
import dk.dma.msinm.model.Point;
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
import java.io.Serializable;
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
    public String createChart(ChartVo chartVo) throws Exception {
        Chart chart = chartVo.toChart();
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
        Chart chart = chartVo.toChart();
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

    /*********************
     * Helper classes
     *********************/

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class ChartVo implements Serializable {
        Integer id;
        String chartNumber;
        Integer internationalNumber;
        String horizontalDatum;


        public Chart toChart() {
            Chart chart = new Chart();
            chart.setId(id);
            chart.setChartNumber(chartNumber);
            chart.setInternationalNumber(internationalNumber);
            chart.setHorizontalDatum(horizontalDatum);
            return chart;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getChartNumber() {
            return chartNumber;
        }

        public void setChartNumber(String chartNumber) {
            this.chartNumber = chartNumber;
        }

        public Integer getInternationalNumber() {
            return internationalNumber;
        }

        public void setInternationalNumber(Integer internationalNumber) {
            this.internationalNumber = internationalNumber;
        }

        public String getHorizontalDatum() {
            return horizontalDatum;
        }

        public void setHorizontalDatum(String horizontalDatum) {
            this.horizontalDatum = horizontalDatum;
        }
    }


}
