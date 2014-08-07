package dk.dma.msinm.reporting;

import dk.dma.msinm.common.mail.MailService;
import dk.dma.msinm.common.model.DataFilter;
import dk.dma.msinm.common.repo.RepoFileVo;
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
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Business interface for accessing MSI-NM reports
 */
@Stateless
public class ReportService extends BaseService {

    public static String REPORT_REPO_FOLDER = "reports";
    private static final DataFilter REPORT_VO_DATA = DataFilter.get("Report.details", "Area.parent");

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
     * Fetches, pre-loads and detaches the report with the given id
     * @param id the id of the report
     * @return the report or null if not found
     */
    public Report fetchReportById(Integer id, DataFilter dataFilter) {
        Report report = getByPrimaryKey(Report.class, id);
        report.preload(dataFilter);
        em.detach(report);
        return report;
    }

    /**
     * Creates a new report from the given template
     * @param reportVo the report template
     * @return the new report
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
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

        em.flush();

        return report;
    }

    /**
     * Computes the list of recipients for a report email
     * @param report the report 
     * @param sendToUser whether to send to the user as well
     * @return the list of recipients
     */
    private String[] getReportMailRecipients(Report report, boolean sendToUser) {
        List<String> recipients = new ArrayList<>();
        if (StringUtils.isNotBlank(reportRecipient)) {
            recipients.add(reportRecipient);
        }
        if (sendToUser) {
            recipients.add(report.getUser().getEmail());
        }
        return recipients.toArray(new String[recipients.size()]);
    }

    /**
     * Sends an email to the authorities and possibly the user
     * @param report the report
     * @param sendToUser whether to send the email to the reporting user or not
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public boolean sendReportEmail(Report report, boolean sendToUser) throws Exception {

        // Refresh the user
        report = getByPrimaryKey(Report.class, report.getId());

        // Send an email to the authorities and possibly the user
        String[] recipients = getReportMailRecipients(report,sendToUser);
        if (recipients.length == 0) {
            return false;
        }

        // Get hold of the list of attachments to be used in the email
        Path reportFolder = getReportRepoFolder(report);
        String uri = repositoryService.getRepoPath(reportFolder);
        List<RepoFileVo> attachments = repositoryService.listFiles(uri);

        User user = report.getUser();

        // Generate the mail HTML
        Map<String, Object> data = new HashMap<>();
        data.put("report", report);
        data.put("attachments", attachments);
        TemplateContext ctx = templateService.getTemplateContext(
                TemplateType.MAIL,
                "report.ftl",
                data,
                user.getLanguage(),
                "Mails");

        // Send the email
        String content = templateService.process(ctx);
        String baseUri = (String) ctx.getData().get("baseUri");

        // Note: The attachments may be large, so, the email links to the attachments
        // and does NOT have them as attachments
        mailService.sendMail(
                content,
                "MSI-NM Report from " + user.getEmail(),
                baseUri,
                recipients);
        return true;
    }

    /**
     * Updates the report status
     * @param id the id of the report
     * @param status the new status
     * @return the updated report
     */
    public Report updateReportStatus(Integer id, ReportStatus status) {
        Report report = getByPrimaryKey(Report.class, id);
        if (report != null && report.getStatus() != status) {
            report.setStatus(status);
            report = saveEntity(report);
        }
        return report;
    }

    /**
     * Returns the list of pending reports
     * @param lang the language to return
     * @return the list of pending reports
     */
    public List<ReportVo> getPendingReports(String lang) {
        // Fetch the list of pending reports
        List<Report> reports = em
                .createNamedQuery("Report.findPendingReports", Report.class)
                .getResultList();

        // Start converting them to ReportVos
        DataFilter filter = new DataFilter(REPORT_VO_DATA).setLang(lang);
        List<ReportVo> result = new ArrayList<>();
        reports.forEach(report -> {
            ReportVo reportVo = new ReportVo(report, filter);

            try {
                // Look up the attachments associated with the report
                Path reportFolder = getReportRepoFolder(report);
                String uri = repositoryService.getRepoPath(reportFolder);
                List<RepoFileVo> attachments = repositoryService.listFiles(uri);
                if (attachments.size() > 0) {
                    reportVo.setAttachments(attachments);
                }
            } catch (IOException e) {
                log.debug("Failed looking up ");
            }

            result.add(reportVo);
        });
        return result;
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

    /**
     * Returns the repository URI for the given report file
     * @param report the report
     * @param name the file name
     * @return the associated repository URI
     */
    public String getReportFileRepoUri(Report report, String name) throws IOException {
        Path file = getReportRepoFolder(report).resolve(name);
        return repositoryService.getRepoUri(file);
    }
}
