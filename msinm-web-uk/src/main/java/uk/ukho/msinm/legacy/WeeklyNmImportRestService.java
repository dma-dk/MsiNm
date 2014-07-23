package uk.ukho.msinm.legacy;

import dk.dma.msinm.common.repo.RepositoryService;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.vo.MessageVo;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.slf4j.Logger;

import javax.ejb.Stateless;
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
public class WeeklyNmImportRestService {
    @Inject
    Logger log;

    @Context
    ServletContext servletContext;

    @Inject
    WeeklyNmImportService weeklyNmImportService;

    /**
     * Imports an uploaded weekly NtM XML file
     *
     * @param request the servlet request
     * @return a status
     */
    @POST
    @Path("/upload-weekly-xml")
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
            Message message = weeklyNmImportService.importMessage(template, weekStartDate, txt);
            if (message != null) {
                result.add(message);
            }
        });

        return result;
    }

}
