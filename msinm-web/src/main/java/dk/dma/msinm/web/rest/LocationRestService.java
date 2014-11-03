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
package dk.dma.msinm.web.rest;

import dk.dma.msinm.common.repo.RepositoryService;
import dk.dma.msinm.model.Location;
import dk.dma.msinm.model.Point;
import dk.dma.msinm.vo.LocationVo;
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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
     * Parses the KML and returns a JSON list of locations.
     *
     * TDOD: Handle MultiGeometry.
     * Example: http://www.microformats.dk/2008/11/02/kommunegrænserne-til-de-98-danske-kommuner/
     *
     * @param kml the KML to parse
     * @return the corresponding list of locations
     */
    @POST
    @Path("/parse-kml")
    @Produces("application/json")
    public List<LocationVo> parseKml(String kml) throws UnsupportedEncodingException {

        // Strip BOM from UTF-8 with BOM
        if (kml.startsWith("\uFEFF")) {
            kml = kml.replace("\uFEFF", "");
        }

        // Extract the default namespace
        String namespace = extractDefaultNamespace(kml);

        List<LocationVo> result = new ArrayList<>();
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new KmlNamespaceContext(namespace));
        InputSource inputSource = new InputSource(new StringReader(kml));

        try {
            // Fetch all "Placemark" elements
            NodeList placemarks = (NodeList) xpath.evaluate("//kml:Placemark", inputSource, XPathConstants.NODESET);

            for (int i = 0; i < placemarks.getLength(); i++) {

                // Fetch all "Point" coordinates
                NodeList coordinates = (NodeList) xpath.evaluate("//kml:Point/kml:coordinates", placemarks.item(i), XPathConstants.NODESET);
                extractLocations(result, coordinates, Location.LocationType.POINT);

                // Fetch all "Polyline" coordinates
                coordinates = (NodeList) xpath.evaluate("//kml:LineString/kml:coordinates", placemarks.item(i), XPathConstants.NODESET);
                extractLocations(result, coordinates, Location.LocationType.POLYLINE);

                // Fetch all "Polygon" coordinates
                coordinates = (NodeList) xpath.evaluate("//kml:Polygon/kml:outerBoundaryIs/kml:LinearRing/kml:coordinates", placemarks.item(i), XPathConstants.NODESET);
                extractLocations(result, coordinates, Location.LocationType.POLYGON);
            }

        } catch (Exception e) {
            log.error("Error parsing kml", e);
        }

        return result;
    }


    /**
     * Converts a list of coordinate nodes into a locations and adds them to the result
     * @param result the result to update with coordinates
     * @param coordinates the coordinates nodes
     * @param type the type of location
     */
    void extractLocations(List<LocationVo> result, NodeList coordinates, Location.LocationType type) {
        if (coordinates != null && coordinates.getLength() > 0) {
            for (int i = 0; i < coordinates.getLength(); i++) {
                LocationVo loc = extractLocation(coordinates.item(i), type);
                if (loc != null) {
                    result.add(loc);
                }
            }
        }
    }

    /**
     * Converts a list of coordinates into a location. Returns null if invalid
     * @param coordinates the coordinates node
     * @param type the type of location
     * @return the location or null
     */
    LocationVo extractLocation(Node coordinates, Location.LocationType type) {
        if (coordinates != null) {
            Location loc = new Location();
            loc.setType(type);

            // Parse the coordinates which consist of white-space separated tuples with one of the following formats:
            // * "longitude,latitude,altitude" (used by Google Earth)
            // * "longitude,latitude"
            String txt = coordinates.getTextContent().trim();
            for (String coord : txt.split("\\s+")) {
                String[] lonLatAlt = coord.split(",");
                if (lonLatAlt.length == 2 || lonLatAlt.length == 3) {
                    Point pt = new Point();
                    pt.setLon(Double.parseDouble(lonLatAlt[0]));
                    pt.setLat(Double.parseDouble(lonLatAlt[1]));
                    pt.setLocation(loc);
                    loc.addPoint(pt);
                }
            }

            if (loc.getPoints().size() > 0) {
                // For polygons, skip the last point since it is identical to the first
                if (loc.getType() == Location.LocationType.POLYGON && loc.getPoints().size() > 1) {
                    loc.getPoints().remove(loc.getPoints().size() - 1);
                }

                return new LocationVo(loc);
            }
        }
        return null;
    }

    /**
     * Annoyingly, different versions of KML use different default namespaces.
     * Hence, attempt to extract the default namespace
     * @param kml the xml
     * @return the default KML namespace
     */
    private String extractDefaultNamespace(String kml) {
        Pattern p = Pattern.compile(".*<kml xmlns=\"([^\"]*)\".*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
        Matcher m = p.matcher(kml);
        if (m.matches()) {
            return m.group(1);
        }
        return "http://www.opengis.net/kml/2.2";
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
    @Produces("application/json;charset=UTF-8")
    public List<LocationVo> uploadKml(@Context HttpServletRequest request) throws FileUploadException, IOException {
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
        return new ArrayList<>();
    }


    /**
     * Defines the KML namespace context
     */
    private static class KmlNamespaceContext implements NamespaceContext {

        String namespace;

        public KmlNamespaceContext(String namespace) {
            this.namespace = namespace;
        }

        public String getNamespaceURI(String prefix) {
            if("kml".equals(prefix)) {
                return namespace;
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
