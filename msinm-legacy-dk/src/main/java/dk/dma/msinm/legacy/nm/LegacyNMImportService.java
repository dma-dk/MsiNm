package dk.dma.msinm.legacy.nm;

import dk.dma.msinm.common.repo.RepositoryService;
import dk.dma.msinm.model.Area;
import dk.dma.msinm.model.Chart;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.model.MessageStatus;
import dk.dma.msinm.model.Priority;
import dk.dma.msinm.service.AreaService;
import dk.dma.msinm.service.ChartService;
import dk.dma.msinm.service.MessageService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
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
                if (item.getName().toLowerCase().endsWith(".pdf")) {
                    List<Message> result = importPDF(item.getInputStream(), item.getName());
                    return "Imported " + result.size() + " legacy NtM messages";
                }
            }
        }

        return "No legacy NtM messages imported";
    }

    /**
     * Imports the NtM PDF file
     * @param pdf the NtM PDF file
     * @return the imported messages
     */
    public List<Message> importPDF(File pdf) throws Exception {
        return importPDF(new FileInputStream(pdf), pdf.getName());
    }

    /**
     * Imports the NtM PDF file
     * @param inputStream the PDF input stream
     * @param fileName the name of the PDF file
     * @return the imported messages
     */
    public List<Message> importPDF(InputStream inputStream, String fileName) throws Exception {

        log.info("Extracting NtM's from PDF " + fileName);

        List<Message> templates = new ArrayList<>();
        NmPdfExtractor extractor = new NmPdfExtractor(inputStream, fileName);
        extractor.extractNotices(templates);
        log.info("Extracted " + templates.size() + " NtM's from " + fileName);

        // Save the template NtM's
        List<Message> result = importMessages(templates);
        log.info("Saved " + result.size() + " out of " + templates.size() + " extracted NtM's from " + fileName);

        return result;
    }

    /**
     * Imports a list of NtM templates and returns the messages actually imported
     * @param templates the NtM templates
     * @return the messages actually imported
     */
    private List<Message> importMessages(List<Message> templates) {

        List<Message> result = new ArrayList<>();

        templates.forEach(template -> {
            Message message = importMessage(template);
            if (message != null) {
                result.add(message);
            }
        });

        return result;
    }

    /**
     * Imports the NtM template and returns the message if it was imported
     * @param template the NtM template
     * @return the message actually imported
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    private Message importMessage(Message template) {
        // Check if the message already exists
        Message message = messageService.findByMessageSeriesId(
                template.getSeriesIdentifier().getNumber(),
                template.getSeriesIdentifier().getYear(),
                template.getSeriesIdentifier().getAuthority()
        );
        if (message != null) {
            log.warn("Message " + template.getSeriesIdentifier() + " alread exists. Skipping");
        }

        // Fill out missing fields
        template.setValidFrom(new Date()); // TODO
        template.setPriority(Priority.NONE);
        template.setStatus(MessageStatus.ACTIVE);

        try {
            // Make sure all charts are saved
            List<Chart> charts = findOrCreateCharts(template.getCharts());
            template.setCharts(charts);

            // Make sure the area, and parent areas, exists
            Area area = findOrCreateArea(template.getArea());
            template.setArea(area);

            // Save the message
            return messageService.create(template);
        } catch (Exception e) {
            log.error("Failed saving message " + message, e);
        }
        return null;
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

