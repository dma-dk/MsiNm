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
package dk.dma.msinm.legacy.nm;

import dk.dma.msinm.common.MsiNmApp;
import dk.dma.msinm.common.repo.RepositoryService;
import dk.dma.msinm.common.util.TextUtils;
import dk.dma.msinm.model.Area;
import dk.dma.msinm.model.Chart;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.model.Priority;
import dk.dma.msinm.model.SeriesIdentifier;
import dk.dma.msinm.model.Status;
import dk.dma.msinm.service.AreaService;
import dk.dma.msinm.service.ChartService;
import dk.dma.msinm.service.MessageService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 */
@Path("/import/legacy-nm-import")
@Stateless
public class LegacyNmImportService {

    @Inject
    Logger log;

    @Context
    ServletContext servletContext;

    @Inject
    MsiNmApp app;

    @Inject
    MessageService messageService;

    @Inject
    AreaService areaService;

    @Inject
    ChartService chartService;

    /**
     * Imports an uploaded NtM PDF file
     *
     * @param request the servlet request
     * @return a status
     */
    @POST
    @javax.ws.rs.Path("/upload-pdf")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json;charset=UTF-8")
    public String importPDF(@Context HttpServletRequest request) throws Exception {
        FileItemFactory factory = RepositoryService.newDiskFileItemFactory(servletContext);
        ServletFileUpload upload = new ServletFileUpload(factory);
        List<FileItem> items = upload.parseRequest(request);

        for (FileItem item : items) {
            if (!item.isFormField()) {
                // NM PDF
                if (NmPdfExtractor.getFileNameMatcher(item.getName()).matches()) {
                    StringBuilder txt = new StringBuilder();
                    importNmPdf(item.getInputStream(), item.getName(), txt);
                    return txt.toString();

                } else if (ActiveTempPrelimNmPdfExtractor.getFileNameMatcher(item.getName()).matches()) {
                    // Active P&T NM PDF
                    StringBuilder txt = new StringBuilder();
                    updateActiveNm(item.getInputStream(), item.getName(), txt);
                    return txt.toString();
                }
            }
        }

        return "No valid PDF uploaded";
    }

    /**
     * Extracts the list of active P&T NM's from the PDF
     * @param inputStream the PDF input stream
     * @param fileName the name of the PDF file
     * @param txt a log of the import
     */
    private void updateActiveNm(InputStream inputStream, String fileName, StringBuilder txt) throws Exception {
        log.info("Extracting active P&T NtM's from PDF " + fileName);

        ActiveTempPrelimNmPdfExtractor extractor = new ActiveTempPrelimNmPdfExtractor(inputStream, fileName, app.getOrganization());

        List<SeriesIdentifier> noticeIds = new ArrayList<>();
        extractor.extractActiveNoticeIds(noticeIds);
        log.info("Extracted " + noticeIds.size() + " active P&T NtM's from " + fileName);
        txt.append("Detected " + noticeIds.size() + " active P&T NtM's in PDF file " + fileName + "\n");

        DateTime weekStartDate =
                new DateTime()
                        .withYear(extractor.getYear())
                        .withDayOfWeek(DateTimeConstants.MONDAY)
                        .withWeekOfWeekyear(extractor.getWeek());

        List<Message> messages = messageService.inactivateTempPrelimNmMessages(noticeIds, weekStartDate.toDate());
        messages.forEach(msg -> {
            txt.append("Inactivate NtM " + msg.getSeriesIdentifier() + "\n");
        });
        log.info("Inactivated " + messages.size());
    }

    /**
     * Imports the NtM PDF file
     * @param pdf the NtM PDF file
     * @param txt a log of the import
     * @return the imported messages
     */
    public List<Message> importNmPdf(File pdf, StringBuilder txt) throws Exception {
        return importNmPdf(new FileInputStream(pdf), pdf.getName(), txt);
    }

    /**
     * Imports the NtM PDF file
     * @param inputStream the PDF input stream
     * @param fileName the name of the PDF file
     * @param txt a log of the import
     * @return the imported messages
     */
    public List<Message> importNmPdf(InputStream inputStream, String fileName, StringBuilder txt) throws Exception {

        log.info("Extracting NtM's from PDF " + fileName);

        List<Message> templates = new ArrayList<>();
        NmPdfExtractor extractor = new NmPdfExtractor(inputStream, fileName, app.getOrganization());
        extractor.extractNotices(templates);
        log.info("Extracted " + templates.size() + " NtM's from " + fileName);
        txt.append("Detected " + templates.size() + " NtM's in PDF file " + fileName + "\n");

        // Save the template NtM's
        DateTime weekStartDate =
                new DateTime()
                        .withYear(extractor.getYear())
                        .withDayOfWeek(DateTimeConstants.MONDAY)
                        .withWeekOfWeekyear(extractor.getWeek());

        List<Message> result = importMessages(templates, weekStartDate.toDate(), txt);
        log.info("Saved " + result.size() + " out of " + templates.size() + " extracted NtM's from " + fileName);
        txt.append("Saved a total of " + result.size() + " NTM's\n");

        return result;
    }

    /**
     * Imports a list of NtM templates and returns the messages actually imported
     * @param templates the NtM templates
     * @param weekStartDate the start date of the week
     * @param txt a log of the import
     * @return the messages actually imported
     */
    private List<Message> importMessages(List<Message> templates, Date weekStartDate, StringBuilder txt) {

        List<Message> result = new ArrayList<>();

        templates.forEach(template -> {
            Message message = importMessage(template, weekStartDate, txt);
            if (message != null) {
                result.add(message);
            }
        });

        return result;
    }

    /**
     * Imports the NtM template and returns the message if it was imported
     * @param template the NtM template
     * @param weekStartDate the start date of the week
     * @param txt a log of the import
     * @return the message actually imported
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    private Message importMessage(Message template, Date weekStartDate, StringBuilder txt) {
        // Check if the message already exists
        Message message = messageService.findBySeriesIdentifier(
                template.getSeriesIdentifier().getNumber(),
                template.getSeriesIdentifier().getYear(),
                template.getSeriesIdentifier().getAuthority()
        );
        if (message != null) {
            log.warn("Message " + template.getSeriesIdentifier() + " already exists. Skipping");
            txt.append("Skipping existing NtM: " + template.getSeriesIdentifier() + "\n");
            return null;
        }

        // Fill out missing fields
        template.setValidFrom(weekStartDate);
        template.setPriority(Priority.NONE);
        template.setStatus(Status.ACTIVE);

        // Some NM's do not have descriptions. Use the (html'ified) title instead
        template.getDescs().forEach(desc -> desc.setDescription(StringUtils.defaultIfBlank(desc.getDescription(), TextUtils.txt2html(desc.getTitle()))));

        try {
            // Make sure all charts are saved
            List<Chart> charts = findOrCreateCharts(template.getCharts());
            template.setCharts(charts);

            // Make sure the area, and parent areas, exists
            Area area = findOrCreateArea(template.getArea());
            template.setArea(area);

            // Save the message
            message = messageService.create(template);
            txt.append("Saved NtM: " + template.getSeriesIdentifier() + "\n");
        } catch (Exception e) {
            txt.append("Error saving NtM: " + template.getSeriesIdentifier() + "\n");
            log.error("Failed saving message " + message, e);
        }
        return message;
    }

    /**
     * Ensures that the template area and it's parents exists
     * @param templateArea the template area
     * @return the area
     */
    //@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    private Area findOrCreateArea(Area templateArea) {
        Area parent = null;
        if (templateArea.getParent() != null) {
            parent = findOrCreateArea(templateArea.getParent());
        }
        Integer parentId = (parent == null) ? null : parent.getId();

        Area area = areaService.findByName(templateArea.getDesc("da").getName(), "da", parentId);
        if (area == null) {
            area = areaService.createArea(templateArea, parentId);
        }
        return area;
    }

    /**
     * Ensures that the template charts area all saved
     * @param templateCharts the template charts
     * @return the actual charts
     */
    //@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    private List<Chart> findOrCreateCharts(List<Chart> templateCharts) {
        List<Chart> result = new ArrayList<>();
        templateCharts.forEach(templateChart -> {
            Chart chart = chartService.findByChartNumber(templateChart.getChartNumber());
            if (chart == null) {
                chart = chartService.createChart(templateChart);
            }
            result.add(chart);
        });
        return result;
    }

}

