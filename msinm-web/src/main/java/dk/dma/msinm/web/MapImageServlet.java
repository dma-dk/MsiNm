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
import dk.dma.msinm.model.Message;
import dk.dma.msinm.model.Point;
import dk.dma.msinm.service.MessageService;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static dk.dma.msinm.model.Location.LocationType;

/**
 * Returns and caches a thumbnail image for a message.
 * <p></p>
 * Can be used e.g. for a grid layout in search results.
 */
@WebServlet(value = "/map-image/*", asyncSupported = true)
public class MapImageServlet extends HttpServlet  {

    private static String STATIC_IMAGE_URL = "http://staticmap.openstreetmap.de/staticmap.php?center=%f,%f&zoom=%d&size=%dx%d";
    private static String IMAGE_PLACEHOLDER = "/img/map_image_placeholder.png";

    private static GlobalMercator mercator = new GlobalMercator();
    private static Image msiImage, nmImage;

    @Inject
    Logger log;

    @Inject
    MessageService messageService;

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
     * Main GET method
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // Strip png path of the request path info to get the id of the message
            int id = Integer.valueOf(request.getPathInfo().substring(1).split("\\.")[0]);

            // Look up the message
            Message message = messageService.getCachedMessage(id);
            if (message == null) {
                throw new IllegalArgumentException("Message " + id + " does not exist");
            }

            List<Location> locations = getMessageLocations(message);
            if (locations.size() > 0) {
                // Construct the image file name for the messsage
                String imageName = String.format("map_%d.png", mapImageSize);

                // Create a hashed sub-folder for the image file
                Path imageRepoPath = messageService.getMessageFileRepoPath(message, imageName);

                // If the image file does not exist or if the message has been updated after the image file
                // generate a new image file
                boolean imageFileExists = Files.exists(imageRepoPath);
                if (!imageFileExists ||
                        message.getUpdated().getTime() > Files.getLastModifiedTime(imageRepoPath).toMillis()) {
                    imageFileExists = createMapImage(message, imageRepoPath);
                }

                // Either return the image file, or a place holder image
                if (imageFileExists) {
                    // Redirect the the repository streaming service
                    String uri = messageService.getMessageFileRepoUri(message, imageName);
                    response.sendRedirect(uri);
                    return;
                }
            }

        } catch (Exception ex) {
            log.warn("Error fetching map image for message: " + ex);
        }

        // Show a placeholder image
        response.sendRedirect(IMAGE_PLACEHOLDER);
    }

    /**
     * Fetches the map image and crops it if specified
     * @param centerPt the center point
     * @param zoom the zoom level
     * @return the image
     */
    private BufferedImage fetchMapImage(Point centerPt, int zoom) throws  IOException {
        // Fetch the image
        long fetchSize = mapImageSize + 2 * mapImageIndent;
        String url = String.format(
                STATIC_IMAGE_URL,
                centerPt.getLat(),
                centerPt.getLon(),
                zoom,
                fetchSize,
                fetchSize);

        BufferedImage image = ImageIO.read(new URL(url));

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
     * Attempts to create a map image for the message at the given path
     * @param message the message
     * @param imageRepoPath the path of the image
     * @return if the image file was properly created
     */
    private boolean createMapImage(Message message, Path imageRepoPath) throws IOException {
        List<Location> locations = getMessageLocations(message);

        if (locations.size() > 0) {

            boolean isSinglePoint = locations.size() == 1 && locations.get(0).getType() == LocationType.POINT;

            // Compute the bounds of the location and compute the center
            Point[] bounds = getBounds(locations);
            Point centerPt = new Point((bounds[0].getLat() + bounds[1].getLat()) / 2.0, (bounds[0].getLon() + bounds[1].getLon()) / 2.0);

            // Find zoom level where polygon is at most 80% of bitmap width/height, and zoom level in
            // the range of 12 to 4. See http://wiki.openstreetmap.org/wiki/Zoom_levels
            int maxWH = (int)(mapImageSize.doubleValue() * 0.8);
            int zoom = (isSinglePoint)
                     ? zoomLevel.intValue()
                     : computeZoomLevel(bounds, maxWH, maxWH, 12, 4);

            // Fetch the image
            BufferedImage image = fetchMapImage(centerPt, zoom);
            Graphics2D g2 = image.createGraphics();
            GraphicsUtils.antialias(g2);
            g2.setStroke(new BasicStroke(2.0f));

            int rx0 = - mapImageSize.intValue() / 2, ry0 = -mapImageSize.intValue() / 2;
            int cxy[] =  mercator.LatLonToPixels(centerPt.getLat(), centerPt.getLon(), zoom);

            // Draw each location
            locations.forEach(loc -> {
                // Point
                if (loc.getType() == LocationType.POINT && loc.getPoints().size() == 1) {
                    int iconSize = 24;

                    Point pt = loc.getPoints().get(0);
                    int xy[] = mercator.LatLonToPixels(pt.getLat(), pt.getLon(), zoom);
                    int px = xy[0] - cxy[0] - rx0 - iconSize / 2;
                    int py = cxy[1] - xy[1] - ry0 - iconSize / 2;

                    g2.drawImage(getMessageImage(message),
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
                    if (loc.getType() == Location.LocationType.POLYGON) {
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
            Files.setLastModifiedTime(imageRepoPath, FileTime.fromMillis(message.getUpdated().getTime()));

            log.info("Saved image for message " + message.getId() + " to file " + imageRepoPath);
            return true;
        }

        return false;
    }

    /**
     * Extracts the locations from the message
     * @param message the message
     * @return the list of locations
     */
    public List<Location> getMessageLocations(Message message) {
        List<Location> result = new ArrayList<>();
        if (message != null) {
            result.addAll(message.getLocations()
                    .stream()
                    .filter(location -> location.getPoints().size() > 0)
                    .collect(Collectors.toList()));
        }
        return result;
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
     * Depending on the type of message, return an MSI or an NM image
     * @param message the  message
     * @return the corresponding image
     */
    public Image getMessageImage(Message message) {
        return message.getType().isMsi() ? getMsiImage() : getNmImage();
    }

    /**
     * Returns the MSI symbol image
     * @return the MSI symbol image
     */
    private synchronized Image getMsiImage() {
        if (msiImage == null) {
            try {
                msiImage = ImageIO.read(new URL("http://localhost:8080/img/msi.png"));
            } catch (IOException e) {
                log.error("This should never happen");
            }
        }
        return msiImage;
    }


    /**
     * Returns the MSI symbol image
     * @return the MSI symbol image
     */
    private synchronized Image getNmImage() {
        if (nmImage == null) {
            try {
                nmImage = ImageIO.read(new URL("http://localhost:8080/img/nm.png"));
            } catch (IOException e) {
                log.error("This should never happen");
            }
        }
        return nmImage;
    }
}
