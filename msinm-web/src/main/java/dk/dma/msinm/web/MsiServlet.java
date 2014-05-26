package dk.dma.msinm.web;

import dk.dma.msinm.common.util.GraphicsUtils;
import dk.dma.msinm.model.MessageLocation;
import dk.dma.msinm.model.Point;
import dk.dma.msinm.service.MessageSearchParams;
import dk.dma.msinm.service.MessageSearchService;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Feeds MSI data as bitmaps
 */
@WebServlet(value = "/msi/*")
public class MsiServlet extends HttpServlet {

    static final int TILE_SIZE = 256;
    static final Pattern TILE_PATTERN = Pattern.compile("/(\\d+)/(\\d+)/(\\d+)\\.png");

    @Inject
    Logger log;

    @Inject
    MessageSearchService messageSearchService;

    /**
     * Constructor
     */
    public MsiServlet() {
        super();
    }

    /**
     * Main GET method
     *
     * @param request  servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Matcher m = TILE_PATTERN.matcher(request.getPathInfo());
        if (!m.matches()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        int z = Integer.parseInt(m.group(1));
        int x = Integer.parseInt(m.group(2));
        int y = Integer.parseInt(m.group(3));

        BufferedImage image = processImage(z, x, y);

        response.setHeader("Cache-Control", "no-cache") ;
        response.setHeader("Expires", "0") ;
        OutputStream out = response.getOutputStream();
        ImageIO.write(image, "png", out);
        image.flush();
        out.close();

    }

    /**
     * Process image
     * @param z the zoom level
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the resulting image
     */
    private BufferedImage processImage(int z, int x, int y) {

        BufferedImage image = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        GraphicsUtils.antialias(g2);

        GlobalMercator mercator = new GlobalMercator();
        double[] bounds = mercator.TileLatLonBounds(x, y, z);

        MessageLocation loc = new MessageLocation();
        loc.setType(MessageLocation.LocationType.POLYGON);
        loc.getPoints().add(new Point(-bounds[0], bounds[1]));
        loc.getPoints().add(new Point(-bounds[2], bounds[1]));
        loc.getPoints().add(new Point(-bounds[2], bounds[3]));
        loc.getPoints().add(new Point(-bounds[0], bounds[3]));

        MessageSearchParams params = new MessageSearchParams();
        params.setMaxHits(10000);
        params.setLocation(loc);

        Color col = Color.red;
        Color fillCol = new Color(1f, 0f, 0f, 0.3f);

        int xy0[] =  mercator.LatLonToPixels(-bounds[0], bounds[1], z);
        java.util.List<MessageLocation> locations = messageSearchService.searchLocations(params);

        locations.stream().forEach(location -> {

            if (location.getType() == MessageLocation.LocationType.POINT) {
                Point pt = location.getPoints().get(0);

                int xy[] = mercator.LatLonToPixels(pt.getLat(), pt.getLon(), z);
                double px = xy[0] - xy0[0];
                double py = -(xy[1] - xy0[1]);
                double radius = 2.0;

                Shape theCircle = new Ellipse2D.Double(px - radius, py - radius, 2.0 * radius, 2.0 * radius);
                g2.setColor(col);
                g2.fill(theCircle);

            } else if (location.getType() == MessageLocation.LocationType.POLYLINE || location.getType() == MessageLocation.LocationType.POLYGON) {
                GeneralPath path = new GeneralPath();
                for (int i = 0; i < location.getPoints().size(); i++) {
                    Point pt = location.getPoints().get(i);
                    int xy[] = mercator.LatLonToPixels(pt.getLat(), pt.getLon(), z);
                    double px = xy[0] - xy0[0];
                    double py = -(xy[1] - xy0[1]);

                    if (i == 0) {
                        path.moveTo(px, py);
                    } else {
                        path.lineTo(px, py);
                    }
                }
                if (location.getType() == MessageLocation.LocationType.POLYGON) {
                    path.closePath();
                    g2.setColor(fillCol);
                    g2.fill(path);
                    g2.setColor(col);
                    g2.draw(path);
                } else {
                    g2.setColor(col);
                    g2.draw(path);
                }

            }
        });

        /*
        LonLat[] bounds = CoordUtils.getLonLatBounds(x, y, z);

        MessageLocation loc = new MessageLocation();
        loc.setType(MessageLocation.LocationType.POLYGON);
        loc.getPoints().add(new Point(bounds[0].getLat(), bounds[0].getLon()));
        loc.getPoints().add(new Point(bounds[1].getLat(), bounds[0].getLon()));
        loc.getPoints().add(new Point(bounds[1].getLat(), bounds[1].getLon()));
        loc.getPoints().add(new Point(bounds[0].getLat(), bounds[1].getLon()));

        MessageSearchParams params = new MessageSearchParams();
        params.setMaxHits(10000);
        params.setLocation(loc);

        g2.setColor(Color.red);

        java.util.List<MessageLocation> locations = messageSearchService.searchLocations(params);
        locations.stream().filter(location -> location.getType() == MessageLocation.LocationType.POINT).forEach(location -> {
            Point pt = location.getPoints().get(0);
            LonLat pos = new LonLat(pt.getLon(), pt.getLat());

            double px = (pos.getLon() - Math.min(bounds[0].getLon(), bounds[1].getLon())) / Math.abs(bounds[0].getLon() - bounds[1].getLon()) * 256.0;
            double py = 256.0 - (pos.getLat() - Math.min(bounds[0].getLat(), bounds[1].getLat())) / Math.abs(bounds[0].getLat() - bounds[1].getLat()) * 256.0;
            double radius = 2.0;

            Shape theCircle = new Ellipse2D.Double(px - radius, py - radius, 2.0 * radius, 2.0 * radius);
            g2.fill(theCircle);
        });

        */

        g2.dispose();
        return image;
    }
}

