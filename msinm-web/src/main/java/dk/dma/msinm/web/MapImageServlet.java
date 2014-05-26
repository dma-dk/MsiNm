package dk.dma.msinm.web;

import dk.dma.msinm.common.repo.RepositoryService;
import dk.dma.msinm.common.settings.annotation.Setting;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.model.MessageLocation;
import dk.dma.msinm.model.NavwarnMessage;
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

import static dk.dma.msinm.common.repo.RepositoryService.HashFolderLevels.ONE;
import static dk.dma.msinm.model.MessageLocation.LocationType;

/**
 * Returns and caches a thumbnail image for a message
 *
 * TODO: Use MD5 hashing folder structure for map image repo
 */
@WebServlet(value = "/map-image/*", asyncSupported = true)
public class MapImageServlet extends HttpServlet  {

    private static String STATIC_IMAGE_URL = "http://staticmap.openstreetmap.de/staticmap.php?center=%f,%f&zoom=%d&size=%dx%d";
    private static String IMAGE_REPO_FOLDER = "map_images";
    private static String IMAGE_PLACEHOLDER = "/img/map_image_placeholder.png";

    private static GlobalMercator mercator = new GlobalMercator();
    private static Image msiImage;

    @Inject
    Logger log;

    @Inject
    RepositoryService repositoryService;

    @Inject
    MessageService messageService;

    @Inject
    @Setting(value = "mapImageSize", defaultValue = "256")
    Long mapImageSize;

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

            List<MessageLocation> locations = getMessageLocations(message);
            if (locations.size() > 0) {
                // Construct the image file name for the messsage
                String imageName = String.format("%d_%d.png", id, mapImageSize);

                // Create a hashed sub-folder for the image file
                Path imageFolder = repositoryService.getHashedSubfolder(ONE, IMAGE_REPO_FOLDER, imageName);
                Path imageRepoPath = imageFolder.resolve(imageName);
                Path imageUrl = repositoryService.getRepoRoot().relativize(imageRepoPath);

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
                    response.sendRedirect("/rest/repo/" + imageUrl.toString());
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
     * Attempts to create a map image for the message at the given path
     * @param message the message
     * @param imageRepoPath the path of the image
     * @return if the image file was properly created
     */
    private boolean createMapImage(Message message, Path imageRepoPath) throws IOException {
        List<MessageLocation> locations = getMessageLocations(message);

        if (locations.size() > 0) {

            // Only handle one for now
            MessageLocation loc = locations.get(0);

            Point[] bounds = getBounds(loc.getPoints());
            Point centerPt = new Point((bounds[0].getLat() + bounds[1].getLat()) / 2.0, (bounds[0].getLon() + bounds[1].getLon()) / 2.0);

            // Find zoom level where polygon is at most 80% of bitmap width/height, and zoom level in
            // the range of 10 to 4. See http://wiki.openstreetmap.org/wiki/Zoom_levels
            int maxWH = (int)(mapImageSize.doubleValue() * 0.8);
            int zoom = (loc.getType() == LocationType.POINT)
                    ? zoomLevel.intValue()
                    : computeZoomLevel(bounds, maxWH, maxWH, 10, 4);

            String url = String.format(STATIC_IMAGE_URL, centerPt.getLat(), centerPt.getLon(), zoom, mapImageSize, mapImageSize);

            BufferedImage image = ImageIO.read(new URL(url));
            Graphics2D g2 = image.createGraphics();
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(
                    RenderingHints.KEY_FRACTIONALMETRICS,
                    RenderingHints.VALUE_FRACTIONALMETRICS_ON);

            int rx0 = -128, ry0 = -128;
            int cxy[] =  mercator.LatLonToPixels(centerPt.getLat(), centerPt.getLon(), zoom);

            if (loc.getType() == LocationType.POINT) {
                g2.drawImage(getMsiImage(), 0 - rx0 - 10, 0 - ry0 - 10, 20, 20, null);

            } else if (loc.getType() == LocationType.POLYGON || loc.getType() == LocationType.POLYLINE) {

                Color col = Color.red;
                Color fillCol = new Color(1f, 0f, 0f, 0.3f);
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
                if (loc.getType() == MessageLocation.LocationType.POLYGON) {
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
    public List<MessageLocation> getMessageLocations(Message message) {
        List<MessageLocation> result = new ArrayList<>();
        if (message != null) {
            NavwarnMessage msg = (NavwarnMessage)message;
            result.addAll(msg.getMessageItems().get(0).getLocations()
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
    public Point[] getBounds(List<Point> points) {
        Point minPt = new Point(90, 180);
        Point maxPt = new Point(-90, -180);
        for (Point pt : points) {
            maxPt.setLat(Math.max(maxPt.getLat(), pt.getLat()));
            maxPt.setLon(Math.max(maxPt.getLon(), pt.getLon()));
            minPt.setLat(Math.min(minPt.getLat(), pt.getLat()));
            minPt.setLon(Math.min(minPt.getLon(), pt.getLon()));
        }

        return new Point[] { minPt, maxPt };
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

}
