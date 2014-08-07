package dk.dma.msinm.web.rest;

import dk.dma.msinm.reporting.Report;
import dk.dma.msinm.reporting.ReportService;
import dk.dma.msinm.reporting.ReportVo;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.List;

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
    @NoCache
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

        Report report = reportService.createReport(reportVo);

        // Send an email
        try {
            reportService.sendReportEmail(report, reportVo.isSendEmail());
        } catch (Exception e) {
            log.warn("Failed sending email for report " + report.getId(), e);
        }

        return "OK";
    }


    /**
     * Returns all pending reports.
     * The operation is restricted to administrators.
     *
     * @param lang the language to return
     * @return the new report template
     */
    @GET
    @Path("/pending-reports")
    @Produces("application/json;charset=UTF-8")
    @NoCache
    @RolesAllowed({ "admin" })
    public List<ReportVo> getPendingReports(@QueryParam("lang") String lang) {
        return reportService.getPendingReports(lang);
    }

    /**
     * Updates the status of the given report. All other changes are discarded
     * @param reportVo the report template
     */
    @PUT
    @Path("/report")
    @Consumes("application/json")
    @Produces("application/json")
    @RolesAllowed({ "admin" })
    public String updateReportStatus(ReportVo reportVo) {

        log.info("Updating status of report " + reportVo.getId() + " to " + reportVo.getStatus());

        reportService.updateReportStatus(reportVo.getId(), reportVo.getStatus());
        return "OK";
    }
}
