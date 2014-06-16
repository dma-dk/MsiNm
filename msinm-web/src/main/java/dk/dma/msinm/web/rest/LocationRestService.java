package dk.dma.msinm.web.rest;

import dk.dma.msinm.common.repo.RepositoryService;
import dk.dma.msinm.model.MessageLocation;
import dk.dma.msinm.model.Point;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Location support
 */
@Path("/location")
public class LocationRestService {

    @Inject
    Logger log;

    @Context
    ServletContext servletContext;

    /**
     * Parses the KML and returns a JSON list of locations
     *
     * @param kml the KML to parse
     * @return the corresponding list of locations
     */
    @POST
    @Path("/parse-kml")
    @Produces("application/json")
    public JsonArray parseKml(String kml) {

        JsonArrayBuilder result = Json.createArrayBuilder();
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new KmlNamespaceContext());
        InputSource inputSource = new InputSource(new StringReader(kml));

        try {
            // Fetch all "Placemark" elements
            NodeList nodes = (NodeList) xpath.evaluate("//kml:Placemark", inputSource, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                MessageLocation loc = new MessageLocation();

                // Extract the name for the Placemark
                Node name = (Node) xpath.evaluate("kml:name", (Node)nodes.item(i), XPathConstants.NODE);
                if (name != null) {
                    //loc.setDescription(name.getTextContent());
                }

                // Try to match either POINT, POLYLINE or POLYGON
                loc.setType(MessageLocation.LocationType.POINT);
                Node coordinates = (Node) xpath.evaluate(
                        "kml:Point/kml:coordinates",
                        (Node)nodes.item(i),
                        XPathConstants.NODE);

                if (coordinates == null) {
                    loc.setType(MessageLocation.LocationType.POLYLINE);
                    coordinates = (Node) xpath.evaluate(
                            "kml:LineString/kml:coordinates",
                            (Node)nodes.item(i),
                            XPathConstants.NODE);
                }
                if (coordinates == null) {
                    loc.setType(MessageLocation.LocationType.POLYGON);
                    coordinates = (Node) xpath.evaluate(
                            "kml:Polygon/kml:outerBoundaryIs/kml:LinearRing/kml:coordinates",
                            (Node)nodes.item(i),
                            XPathConstants.NODE);
                }

                if (coordinates != null) {
                    // Parse the coordinates which has the format of tuples, "longitude,latitude,altitude", separated by whitespace
                    String txt = coordinates.getTextContent().trim();
                    for (String coord : txt.split("\\s+")) {
                        String[] lonLatAlt = coord.split(",");
                        if (lonLatAlt.length == 3) {
                            Point pt = new Point();
                            pt.setLon(Double.parseDouble(lonLatAlt[0]));
                            pt.setLat(Double.parseDouble(lonLatAlt[1]));
                            loc.addPoint(pt);
                        }
                    }

                    if (loc.getPoints().size() > 0) {
                        // For polygons, skip the last point since it is identical to the first
                        if (loc.getType() == MessageLocation.LocationType.POLYGON && loc.getPoints().size() > 1) {
                            loc.getPoints().remove(loc.getPoints().size() - 1);
                        }

                        result.add(loc.toJson());
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error parsing kml", e);
        }

        return result.build();
    }

    /**
     * Parse the KML file of the uploaded .kmz or .kml file and returns a JSON list of locations
     *
     * @param request the servlet request
     * @return the corresponding list of locations
     */
    @POST
    @javax.ws.rs.Path("/upload-kml")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    public JsonArray uploadKml(@Context HttpServletRequest request) throws FileUploadException, IOException {
        FileItemFactory factory = RepositoryService.newDiskFileItemFactory(servletContext);
        ServletFileUpload upload = new ServletFileUpload(factory);
        List<FileItem> items = upload.parseRequest(request);

        for (FileItem item : items) {
            if (!item.isFormField()) {
                try {
                    // .kml file
                    if (item.getName().toLowerCase().endsWith(".kml")) {
                        // Parse the KML and return the corresponding locations
                        return parseKml(IOUtils.toString(item.getInputStream()));
                    }

                    // .kmz file
                    else if (item.getName().toLowerCase().endsWith(".kmz")) {
                        // Parse the .kmz file as a zip file
                        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(item.getInputStream()));
                        ZipEntry entry;

                        // Look for the first zip entry with a .kml extension
                        while ((entry = zis.getNextEntry()) != null) {
                            if (!entry.getName().toLowerCase().endsWith(".kml")) {
                                continue;
                            }

                            log.info("Unzipping: " + entry.getName());
                            int size;
                            byte[] buffer = new byte[2048];
                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                            while ((size = zis.read(buffer, 0, buffer.length)) != -1) {
                                bytes.write(buffer, 0, size);
                            }
                            bytes.flush();
                            zis.close();

                            // Parse the KML and return the corresponding locations
                            return parseKml(new String(bytes.toByteArray(), "UTF-8"));
                        }
                    }

                } catch (Exception ex) {
                    log.error("Error extracting kmz", ex);
                }
            }
        }

        // Return an empty result
        return Json.createArrayBuilder().build();
    }


    /**
     * Defines the KML namespace context
     */
    private static class KmlNamespaceContext implements NamespaceContext {

        public String getNamespaceURI(String prefix) {
            if("kml".equals(prefix)) {
                return "http://www.opengis.net/kml/2.2";
            }
            return null;
        }

        public String getPrefix(String namespaceURI) {
            return null;
        }

        public Iterator getPrefixes(String namespaceURI) {
            return null;
        }

    }
}
