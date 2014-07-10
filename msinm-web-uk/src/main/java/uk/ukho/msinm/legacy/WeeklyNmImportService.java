package uk.ukho.msinm.legacy;

import dk.dma.msinm.common.MsiNmApp;
import dk.dma.msinm.common.repo.RepositoryService;
import dk.dma.msinm.service.AreaService;
import dk.dma.msinm.service.ChartService;
import dk.dma.msinm.service.MessageService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
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
import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
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
                    importNM(item.getInputStream(), item.getName(), txt);
                    return txt.toString();
                }
            }
        }

        log.info("No valid XML uploaded");
        return "No valid XML uploaded";
    }

    private void importNM(InputStream inputStream, String name, StringBuilder txt) {

        try (InputStream xsl = getClass().getResourceAsStream("/weekly.xsl")){
            // First step is to perform an XSLT into a messages format
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(new StreamSource(xsl));
            StringWriter result = new StringWriter();
            transformer.transform(new StreamSource(inputStream), new StreamResult(result));

            System.out.println("RESULT:\n" + result.toString());

            // Read the messages xml document using JAXB
            Messages messages = Messages.fromXml(new StringReader(result.toString()));
            log.info("Extracted " + messages.getMessages().size() + " messages");
            txt.append(name + " contains " + messages.getMessages().size() + " T&P NM's %n");
        } catch (Exception e) {
            e.printStackTrace();
            txt.append("Error parsing " + name + ": " + e.getMessage() + "%n");
        }
    }
}
