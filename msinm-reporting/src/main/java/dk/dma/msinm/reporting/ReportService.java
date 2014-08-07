package dk.dma.msinm.reporting;

import dk.dma.msinm.common.mail.MailService;
import dk.dma.msinm.common.repo.RepositoryService;
import dk.dma.msinm.common.service.BaseService;
import dk.dma.msinm.common.settings.annotation.Setting;
import dk.dma.msinm.common.templates.TemplateContext;
import dk.dma.msinm.common.templates.TemplateService;
import dk.dma.msinm.common.templates.TemplateType;
import dk.dma.msinm.model.Area;
import dk.dma.msinm.service.AreaService;
import dk.dma.msinm.user.User;
import dk.dma.msinm.user.UserService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Business interface for accessing MSI-NM reports
 */
@Stateless
public class ReportService extends BaseService {

    public static String REPORT_REPO_FOLDER = "reports";

    @Resource
    SessionContext ctx;

    @Inject
    Logger log;

    @Inject
    UserService userService;

    @Inject
    AreaService areaService;

    @Inject
    RepositoryService repositoryService;

    @Inject
    MailService mailService;

    @Inject
    TemplateService templateService;

    @Inject
    @Setting(value = "reportRecipient", defaultValue = "peder@carolus.dk") // TODO: Change
    String reportRecipient;


    /**
     * Creates a new report template with a temporary repository path
     * @return the new report template
     */
    public ReportVo newTemplateReport() {
        ReportVo reportVo = new ReportVo();

        reportVo.setLocations(new ArrayList<>());
        reportVo.setRepoPath(repositoryService.getNewTempDir().getPath());
        return  reportVo;
    }

    /**
     * Creates a new report from the given template
     * @param reportVo the report template
     * @return the new report
     */
    public Report createReport(ReportVo reportVo) throws Exception {

        // Check the calling principal
        if (ctx.getCallerPrincipal() == null) {
            throw new SecurityException("Invalid user " + ctx.getCallerPrincipal());
        }

        // Look up the user
        User user = userService.findByPrincipal(ctx.getCallerPrincipal());
        if (user == null) {
            // Should never happen
            throw new SecurityException("Invalid user " + ctx.getCallerPrincipal());
        }

        // Create a report entity from the template
        Report report = reportVo.toEntity();
        report.setUser(user);
        report.setStatus(ReportStatus.PENDING);

        if (StringUtils.isNotBlank(reportVo.getAreaId())) {
            Area area = getByPrimaryKey(Area.class, Integer.valueOf(reportVo.getAreaId()));
            report.setArea(area);
        }

        // Save the report
        report = saveEntity(report);
        log.info("Saved report " + report);

        // Move the temporary repo folder to the final destination
        String repoPath = repositoryService.getRepoPath(getReportRepoFolder(report));
        log.info("Moving repo from " + reportVo.getRepoPath() + " to " + repoPath);
        boolean repoMoved = repositoryService.moveRepoFolder(reportVo.getRepoPath(), repoPath);

        // Update the description with the new path
        if (repoMoved &&
                StringUtils.isNotBlank(report.getDescription()) &&
                report.getDescription().contains(reportVo.getRepoPath())) {
            report.setDescription(report.getDescription().replaceAll(reportVo.getRepoPath(), repoPath));
            // Save the updated description
            report = saveEntity(report);
        }

        // Send an email to the authorities and possibly the user
        try {
            sendEmail(report, reportVo.isSendEmail());
        } catch (Exception e) {
            log.warn("Failed sending email for report " + report, e);
        }

        return report;
    }

    /**
     * Sends an email to the authorities and possibly the user
     * @param report the report
     * @param sendToUser whether to send to the user as well
     */
    private void sendEmail(Report report, boolean sendToUser) throws Exception {

        User user = report.getUser();

        // Generate the mail HTML
        Map<String, Object> data = new HashMap<>();
        data.put("report", report);
        TemplateContext ctx = templateService.getTemplateContext(
                TemplateType.MAIL,
                "report.ftl",
                data,
                user.getLanguage(),
                "Mails");

        // Determine recipients
        String[] recipients = (sendToUser)
                ? new String[] { reportRecipient, user.getEmail() }
                : new String[] { reportRecipient };

        // Send the email
        String content = templateService.process(ctx);
        String baseUri = (String)ctx.getData().get("baseUri");
        // TODO: Create mail with attachments...
        mailService.sendMail(
                content,
                "MSI-NM Report from " + user.getEmail(),
                baseUri,
                recipients);
    }

    /***************************************/
    /** Repo methods                      **/
    /***************************************/

    /**
     * Returns the repository folder for the given report
     * @param id the id of the report
     * @return the associated repository folder
     */
    public Path getReportRepoFolder(Integer id) throws IOException {
        return  repositoryService.getHashedSubfolder(REPORT_REPO_FOLDER, String.valueOf(id), true);
    }

    /**
     * Returns the repository folder for the given message
     * @param report the report
     * @return the associated repository folder
     */
    public Path getReportRepoFolder(Report report) throws IOException {
        return  getReportRepoFolder(report.getId());
    }

}
