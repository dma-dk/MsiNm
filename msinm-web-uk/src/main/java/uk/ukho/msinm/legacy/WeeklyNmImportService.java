package uk.ukho.msinm.legacy;

import dk.dma.msinm.common.MsiNmApp;
import dk.dma.msinm.common.repo.RepositoryService;
import dk.dma.msinm.model.*;
import dk.dma.msinm.service.AreaService;
import dk.dma.msinm.service.CategoryService;
import dk.dma.msinm.service.ChartService;
import dk.dma.msinm.service.MessageService;
import dk.dma.msinm.vo.MessageVo;
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
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 */
@Path("/import/legacy-nm-import")
@Stateless
public class WeeklyNmImportService {
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
    CategoryService categoryService;

    @Inject
    ChartService chartService;

    /**
     * Imports an uploaded weekly NtM XML file
     *
     * @param request the servlet request
     * @return a status
     */
    @POST
    @javax.ws.rs.Path("/upload-weekly-xml")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json;charset=UTF-8")
    public String importWeeklyXml(@Context HttpServletRequest request) throws Exception {
        log.info("Starting import of weekly NM xml");

        FileItemFactory factory = RepositoryService.newDiskFileItemFactory(servletContext);
        ServletFileUpload upload = new ServletFileUpload(factory);
        List<FileItem> items = upload.parseRequest(request);

        Integer year = null, week = null;
        for (FileItem item : items) {
            if (item.isFormField()) {
                if (item.getFieldName().equals("data")) {
                    try (JsonReader jsonReader = Json.createReader(new StringReader(item.getString()))) {
                        JsonObject jsonObject = jsonReader.readObject();
                        year = jsonObject.getInt("year");
                        week = jsonObject.getInt("week");
                    }
                }
            }
        }
        if (year == null || week == null) {
            log.warn("Missing year or week");
            return "Missing year or week";
        }
        log.info(String.format("Detected year %d, week %d", year, week));

        for (FileItem item : items) {
            if (!item.isFormField()) {
                if (item.getName().toLowerCase().endsWith(".xml")) {
                    // Found an uploaded xml file
                    StringBuilder txt = new StringBuilder();
                    importNM(item.getInputStream(), item.getName(), txt, year, week);
                    return txt.toString();
                }
            }
        }

        log.info("No valid XML uploaded");
        return "No valid XML uploaded";
    }

    private void importNM(InputStream inputStream, String name, StringBuilder txt, int year, int week) {

        try (InputStream xsl = getClass().getResourceAsStream("/weekly.xsl")){
            // First step is to perform an XSLT into a messages format
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(new StreamSource(xsl));
            StringWriter xsltResult = new StringWriter();
            transformer.transform(new StreamSource(inputStream), new StreamResult(xsltResult));

            // Read the messages xml document using JAXB
            Messages templates = Messages.fromXml(new StringReader(xsltResult.toString()));
            log.info("Extracted " + templates.getMessages().size() + " messages");
            txt.append(name + " contains " + templates.getMessages().size() + " T&P NM's %n");

            // Save the template NtM's
            DateTime weekStartDate =
                    new DateTime()
                            .withYear(year)
                            .withDayOfWeek(DateTimeConstants.MONDAY)
                            .withWeekOfWeekyear(week);

            List<Message> result = importMessages(templates.getMessages(), weekStartDate.toDate(), txt);

            log.info("Saved " + result.size() + " out of " + templates.getMessages().size() + " extracted NtM's from " + name);
            txt.append("Saved a total of " + result.size() + " NTM's\n");

        } catch (Exception e) {
            e.printStackTrace();
            txt.append("Error parsing " + name + ": " + e.getMessage() + "%n");
        }
    }

    /**
     * Imports a list of NtM templates and returns the messages actually imported
     * @param templates the NtM templates
     * @param weekStartDate the start date of the week
     * @param txt a log of the import
     * @return the messages actually imported
     */
    private List<Message> importMessages(List<MessageVo> templates, Date weekStartDate, StringBuilder txt) {

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
    private Message importMessage(MessageVo template, Date weekStartDate, StringBuilder txt) {
        // Check if the message already exists
        Message existingMessage = messageService.findBySeriesIdentifier(
                template.getSeriesIdentifier().getMainType(),
                template.getSeriesIdentifier().getNumber(),
                template.getSeriesIdentifier().getYear(),
                template.getSeriesIdentifier().getAuthority()
        );
        if (existingMessage != null) {
            log.warn("Message " + template.getSeriesIdentifier() + " already exists. Skipping");
            txt.append("Skipping existing NtM: " + template.getSeriesIdentifier() + "\n");
            return null;
        }

        // Fill out missing fields
        Message message = template.toEntity();
        message.setValidFrom(weekStartDate);
        message.setPriority(Priority.NONE);
        message.setStatus(Status.PUBLISHED);

        // Trim the NM titles, as the XSLT has left them with newlines, etc
        message.getDescs().stream()
                .filter(desc -> StringUtils.isNotBlank(desc.getTitle()))
                .forEach(desc -> desc.setTitle(desc.getTitle().replaceAll("\\s+", " ")));

        // Some NM's do not have descriptions. Use the title instead
        message.getDescs()
                .forEach(desc -> desc.setDescription(StringUtils.defaultIfBlank(desc.getDescription(), desc.getTitle())));

        // Update the location type, depending on the number of points
        // By default, they have been set to be polygons.
        message.getLocations().forEach(loc -> {
            if (loc.getPoints().size() == 1) {
                loc.setType(Location.LocationType.POINT);
            } else if (loc.getPoints().size() == 2) {
                loc.setType(Location.LocationType.POLYLINE);
            }
        });

        try {
            // Make sure all charts are saved
            List<Chart> charts = findOrCreateCharts(message.getCharts());
            message.setCharts(charts);

            // Make sure the area, and parent areas, exists
            Area area = findOrCreateArea(message.getArea());
            message.setArea(area);

            // Make sure the categories are saved. NB: UK only use one level of categories
            List<Category> categories = findOrCreateCategories(message.getCategories());
            message.setCategories(categories);

            // Save the message
            message = messageService.create(message);
            txt.append("Saved NtM: " + message.getSeriesIdentifier() + "\n");
        } catch (Exception e) {
            txt.append("Error saving NtM: " + message.getSeriesIdentifier() + "\n");
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

        Area area = areaService.findByName(templateArea.getDesc("en").getName(), "en", parentId);
        if (area == null) {
            area = new Area();
            area.copyDescs(templateArea.getDescs());
            area.setLocations(templateArea.getLocations());
            area = areaService.createArea(area, parentId);
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

    /**
     * Ensures that the template categories are all saved
     * NB: We know that UK only use one level of categories!
     * @param templateCategories the template categories
     * @return the actual categories
     */
    //@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    private List<Category> findOrCreateCategories(List<Category> templateCategories) {
        List<Category> result = new ArrayList<>();
        templateCategories.forEach(templateCategory -> {
            Category category = categoryService.findByName(templateCategory.getDesc("en").getName(), "en", null);
            if (category == null) {
                category = new Category();
                category.copyDescs(templateCategory.getDescs());
                category = categoryService.createCategory(category, null);
            }
            result.add(category);
        });
        return result;
    }

}
