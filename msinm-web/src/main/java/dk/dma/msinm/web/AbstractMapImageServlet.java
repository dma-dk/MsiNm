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
package dk.dma.msinm.web;

import dk.dma.msinm.common.settings.annotation.Setting;
import dk.dma.msinm.common.util.GraphicsUtils;
import dk.dma.msinm.model.Location;
import dk.dma.msinm.model.Point;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.http.HttpServlet;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static dk.dma.msinm.model.Location.LocationType;

/**
 * Returns and caches a thumbnail image for given locations.
 */
public abstract class AbstractMapImageServlet extends HttpServlet  {

    static final String STATIC_IMAGE_URL = "http://staticmap.openstreetmap.de/staticmap.php?center=%f,%f&zoom=%d&size=%dx%d";
    static final String IMAGE_PLACEHOLDER = "/img/map_image_placeholder.png";

    static GlobalMercator mercator = new GlobalMercator();

    @Inject
    Logger log;

    @Inject
    @Setting(value = "mapImageSize", defaultValue = "256")
    Long mapImageSize;

    @Inject
    @Setting(value = "mapImageIndent", defaultValue = "22")
    Long mapImageIndent;

    @Inject
    @Setting(value = "mapImageZoomLevel", defaultValue = "8")
    Long zoomLevel;

    /**
     * Fetches the map image and crops it if specified
     * @param centerPt the center point
     * @param zoom the zoom level
     * @return the image
     */
    protected BufferedImage fetchMapImage(Point centerPt, int zoom) throws  IOException {
        // Fetch the image
        long fetchSize = mapImageSize + 2 * mapImageIndent;
        String url = String.format(
                STATIC_IMAGE_URL,
                centerPt.getLat(),
                centerPt.getLon(),
                zoom,
                fetchSize,
                fetchSize);

        URLConnection con = new URL(url).openConnection();
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);

        BufferedImage image;
        try (InputStream in = con.getInputStream()) {
            image = ImageIO.read(in);
        }

        // Check if we need to crop the image (e.g. to remove watermarks)
        if (mapImageIndent > 0) {
            // NB: sub-images share the same image buffer as the source image
            image = image.getSubimage(
                    mapImageIndent.intValue(),
                    mapImageIndent.intValue(),
                    mapImageSize.intValue(),
                    mapImageSize.intValue());
        }

        return image;
    }

    /**
     * Attempts to create a map image for the locations at the given path
     * @param locations the locations
     * @param imageRepoPath the path of the image
     * @param pointIndicator the image to use as a point indicator
     * @param imageDate the date to set on newly create images
     * @return if the image file was properly created
     */
    protected boolean createMapImage(List<Location> locations, Path imageRepoPath, Image pointIndicator, Date imageDate) throws IOException {

        if (locations.size() > 0) {

            // Convert circles into polygons
            locations = convertLocations(locations);

            boolean isSinglePoint = locations.size() == 1 && locations.get(0).getType() == LocationType.POINT;

            // Compute the bounds of the location and compute the center
            Point[] bounds = getBounds(locations);
            Point centerPt = new Point((bounds[0].getLat() + bounds[1].getLat()) / 2.0, (bounds[0].getLon() + bounds[1].getLon()) / 2.0);

            // Find zoom level where polygon is at most 80% of bitmap width/height, and zoom level in
            // the range of 12 to 4. See http://wiki.openstreetmap.org/wiki/Zoom_levels
            int maxWH = (int) (mapImageSize.doubleValue() * 0.8);
            int zoom = (isSinglePoint)
                    ? zoomLevel.intValue()
                    : computeZoomLevel(bounds, maxWH, maxWH, 12, 3);

            // Fetch the image
            BufferedImage image = fetchMapImage(centerPt, zoom);
            Graphics2D g2 = image.createGraphics();
            GraphicsUtils.antialias(g2);
            g2.setStroke(new BasicStroke(2.0f));

            int rx0 = -mapImageSize.intValue() / 2, ry0 = -mapImageSize.intValue() / 2;
            int cxy[] = mercator.LatLonToPixels(centerPt.getLat(), centerPt.getLon(), zoom);

            // Draw each location
            locations.forEach(loc -> {
                // Point
                if (loc.getType() == LocationType.POINT && loc.getPoints().size() == 1) {
                    int iconSize = 24;

                    Point pt = loc.getPoints().get(0);
                    int xy[] = mercator.LatLonToPixels(pt.getLat(), pt.getLon(), zoom);
                    int px = xy[0] - cxy[0] - rx0 - iconSize / 2;
                    int py = cxy[1] - xy[1] - ry0 - iconSize / 2;

                    g2.drawImage(pointIndicator,
                            px,
                            py,
                            iconSize,
                            iconSize,
                            null);

                } else if (loc.getType() == LocationType.POLYGON || loc.getType() == LocationType.POLYLINE) {
                    // Polygon / polyline

                    Color col = new Color(143, 47, 123);
                    Color fillCol = new Color(173, 87, 161, 80);
                    GeneralPath path = new GeneralPath();
                    for (int i = 0; i < loc.getPoints().size(); i++) {
                        Point pt = loc.getPoints().get(i);
                        int xy[] = mercator.LatLonToPixels(pt.getLat(), pt.getLon(), zoom);

                        double px = xy[0] - cxy[0] - rx0;
                        double py = cxy[1] - xy[1] - ry0;

                        if (i == 0) {
                            path.moveTo(px, py);
                        } else {
                            path.lineTo(px, py);
                        }
                    }
                    if (loc.getType() == LocationType.POLYGON) {
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

            g2.dispose();

            // Save the image to the repository
            ImageIO.write(image, "png", imageRepoPath.toFile());
            image.flush();

            // Update the timestamp of the image file to match the change date of the message
            Files.setLastModifiedTime(imageRepoPath, FileTime.fromMillis(imageDate.getTime()));

            log.info("Saved image for to file " + imageRepoPath);
            return true;
        }

        return false;
    }

    /**
     * Computes the zoom level based on the bitmap size and the bounds
     * @param bounds the bounds
     * @param maxWidth the maximum bitmap width
     * @param maxHeight the maximum bitmap height
     * @param maxZoomLevel  the maximum zoom level
     * @param minZoomLevel the minimum zoom level
     * @return the optimal zoom level
     */
    private int computeZoomLevel(Point[] bounds, int maxWidth, int maxHeight, int maxZoomLevel, int minZoomLevel) {
        for (int zoom = maxZoomLevel; zoom > minZoomLevel; zoom--) {
            int xy0[] = mercator.LatLonToPixels(bounds[0].getLat(), bounds[0].getLon(), zoom);
            int xy1[] = mercator.LatLonToPixels(bounds[1].getLat(), bounds[1].getLon(), zoom);
            if (xy1[0] - xy0[0] <= maxWidth && xy1[1] - xy0[1] <= maxHeight) {
                return zoom;
            }
        }
        return minZoomLevel;
    }

    /**
     * Calculate the bounds
     * TODO: Cater with border cases...
     */
    public Point[] getBounds(List<Location> locations) {
        Point minPt = new Point(90, 180);
        Point maxPt = new Point(-90, -180);
        locations.forEach(loc -> loc.getPoints().forEach(pt -> {
                    maxPt.setLat(Math.max(maxPt.getLat(), pt.getLat()));
                    maxPt.setLon(Math.max(maxPt.getLon(), pt.getLon()));
                    minPt.setLat(Math.min(minPt.getLat(), pt.getLat()));
                    minPt.setLon(Math.min(minPt.getLon(), pt.getLon()));
                }
        ));

        return new Point[] { minPt, maxPt };
    }

    /**
     * Substitutes all circles in the list with polygons
     * @param locations the locations to convert
     * @return the locations sans circles
     */
    private List<Location> convertLocations(List<Location> locations) {
        return locations.stream()
                .map(loc -> loc.getType() == LocationType.CIRCLE ? circle2polygon(loc, 20) : loc)
                .collect(Collectors.toList());
    }

    /**
     * Converts a circle into a polygon with the specified number of points
     * @param circle the circle
     * @param noPoints the number of points
     * @return the corresponding polygon
     */
    private Location circle2polygon(Location circle, int noPoints) {
        // Sanity checks
        if (circle == null || circle.getType() != LocationType.CIRCLE ||
                circle.getRadius() == null || circle.getPoints().size() != 1) {
            return circle;
        }

        Point center = circle.getPoints().get(0);
        Location polygon = new Location();
        polygon.setType(LocationType.POLYGON);

        double lat1 = Math.toRadians(center.getLat());
        double lon1 = Math.toRadians(center.getLon());
        double R = 6371.0087714; // earths mean radius
        double d = circle.getRadius().doubleValue() * 1852.0 / 1000.0; // nm -> km
        for (int i = 0; i < noPoints; i++) {
            double brng = Math.PI * 2 * i / noPoints;
            double lat2 = Math.asin( Math.sin(lat1)*Math.cos(d/R) +
                    Math.cos(lat1)*Math.sin(d/R)*Math.cos(brng) );
            double lon2 = lon1 + Math.atan2(Math.sin(brng)*Math.sin(d/R)*Math.cos(lat1),
                    Math.cos(d/R)-Math.sin(lat1)*Math.sin(lat2));

            polygon.getPoints().add(new Point(Math.toDegrees(lat2), Math.toDegrees(lon2)));
        }
        return polygon;
    }

}
