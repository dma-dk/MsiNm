package dk.dma.msinm.web.rest;

import dk.dma.msinm.reporting.ReportService;
import dk.dma.msinm.reporting.ReportVo;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.slf4j.Logger;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * REST interface for accessing MSI-NM reports
 */
@Path("/reports")
@Stateless
@SecurityDomain("msinm-policy")
@RolesAllowed({ "user" })
public class ReportRestService {

    @Inject
    Logger log;

    @Inject
    ReportService reportService;

    /**
     * Creates a new report template with a temporary repository path
     * @return the new report template
     */
    @GET
    @Path("/new-report-template")
    @Produces("application/json;charset=UTF-8")
    public ReportVo newTemplateReport() {
        return reportService.newTemplateReport();
    }

    /**
     * Creates a report based on the report template
     * @param reportVo the report template
     */
    @POST
    @Path("/report")
    @Consumes("application/json")
    @Produces("application/json")
    public String createReport(ReportVo reportVo) throws Exception {

        log.info("Creating report " + reportVo);

        reportService.createReport(reportVo);
        return "OK";
    }


}
