package dk.dma.msinm.web.rest;

import dk.dma.msinm.model.MessageLocation;
import dk.dma.msinm.model.Point;
import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.Iterator;

/**
 * Location support
 */
@Path("/location")
public class LocationRestService {

    @Inject
    Logger log;

    @POST
    @Path("/parse-kml")
    @Produces("application/json")
    public JsonArray resetPassword(String kml) {

        JsonArrayBuilder result = Json.createArrayBuilder();
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new KmlNamespaceContext());
        InputSource inputSource = new InputSource(new StringReader(kml));

        try {
            NodeList nodes = (NodeList) xpath.evaluate("//kml:Placemark", inputSource, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                MessageLocation loc = new MessageLocation();
                Node name = (Node) xpath.evaluate("kml:name", (Node)nodes.item(i), XPathConstants.NODE);
                if (name != null) {
                    //loc.setDescription(name.getTextContent());
                }

                loc.setType(MessageLocation.LocationType.POINT);
                Node coordinates = (Node) xpath.evaluate("kml:Point/kml:coordinates", (Node)nodes.item(i), XPathConstants.NODE);
                if (coordinates == null) {
                    loc.setType(MessageLocation.LocationType.POLYLINE);
                    coordinates = (Node) xpath.evaluate("kml:LineString/kml:coordinates", (Node)nodes.item(i), XPathConstants.NODE);
                }
                if (coordinates == null) {
                    loc.setType(MessageLocation.LocationType.POLYGON);
                    coordinates = (Node) xpath.evaluate("kml:Polygon/kml:outerBoundaryIs/kml:LinearRing/kml:coordinates", (Node)nodes.item(i), XPathConstants.NODE);
                }

                if (coordinates != null) {
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
