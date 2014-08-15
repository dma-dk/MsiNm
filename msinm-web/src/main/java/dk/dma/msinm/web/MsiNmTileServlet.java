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

import dk.dma.msinm.common.repo.RepositoryService;
import dk.dma.msinm.common.util.GraphicsUtils;
import dk.dma.msinm.common.util.WebUtils;
import dk.dma.msinm.model.Location;
import dk.dma.msinm.model.Point;
import dk.dma.msinm.service.MessageSearchParams;
import dk.dma.msinm.service.MessageSearchResult;
import dk.dma.msinm.service.MessageSearchService;
import dk.dma.msinm.vo.LocationVo;
import dk.dma.msinm.vo.MessageVo;
import dk.dma.msinm.vo.PointVo;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Feeds MSI-NM data as bitmaps.
 * Can be used for servicing an OpenStreetmapLayer in Openlayers.
 * The layer should be configured to have the url "/msinm-tiles/${z}/${x}/${y}.png"
 */
@WebServlet(value = "/msinm-tiles/*")
public class MsiNmTileServlet extends HttpServlet {

    static final int        TILE_SIZE           = 256;
    static final Pattern    TILE_PATTERN        = Pattern.compile("/(\\d+)/(\\d+)/(\\d+)\\.png");
    static final int        TILE_TTL_HOURS      = 24; // A tile file is refreshed every hour...
    static final String     TILE_REPO_FOLDER    = "tiles";
    static final String     BLANK_IMAGE         = "/img/blank.png";

    @Inject
    Logger log;

    @Inject
    MessageSearchService messageSearchService;

    @Inject
    MsiNmTileCache tileCache;

    @Inject
    RepositoryService repositoryService;

    /**
     * Constructor
     */
    public MsiNmTileServlet() {
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

        String path = request.getPathInfo();

        // Firstly, check if the url is cached
        if (tileCache.getCache().containsKey(path)) {
            redirect(response, tileCache.getCache().get(path), true);
            return;
        }

        // Check the the path matches the tile pattern
        Matcher m = TILE_PATTERN.matcher(path);
        if (!m.matches()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        int z = Integer.parseInt(m.group(1));
        int x = Integer.parseInt(m.group(2));
        int y = Integer.parseInt(m.group(3));

        // If the message index has not indexed all messages, return a blank image
        if (!messageSearchService.isAllIndexed()) {
            redirect(response, BLANK_IMAGE, false);
            return;
        }

        // Next, check if the tile exists in the repository
        Path file = repositoryService.getRepoRoot().resolve(TILE_REPO_FOLDER + path);
        String repoUri = "/rest/repo/file/" + TILE_REPO_FOLDER + path;
        if (Files.exists(file) &&
                Files.getLastModifiedTime(file).toMillis() > System.currentTimeMillis() - TILE_TTL_HOURS * 60 * 60 * 1000L) {
            log.debug("Using cached tile for path " + path);
            tileCache.getCache().put(path, repoUri);
            redirect(response, repoUri, true);
            return;
        }

        // Search all messages in the bounds of the tile
        long t0 = System.currentTimeMillis();
        GlobalMercator mercator = new GlobalMercator();
        double[] bounds = mercator.TileLatLonBounds(x, y, z);
        java.util.List<LocationVo> locations = searchLocations(bounds);

        // If the search result is empty, return blank and cache the result
        if (locations.isEmpty()) {
            tileCache.getCache().put(path, BLANK_IMAGE);
            redirect(response, BLANK_IMAGE, true);
            return;
        }

        // Generate an image
        BufferedImage image = processImage(z, bounds, mercator, locations);

        // Write the image to the repository
        if (!Files.exists(file.getParent())) {
            Files.createDirectories(file.getParent());
        }
        ImageIO.write(image, "png", file.toFile());

        // Cache the resulting path and redirect the response
        tileCache.getCache().put(path, repoUri);
        redirect(response, repoUri, true);

        log.debug("Generated " + path + " in " + (System.currentTimeMillis() - t0) + " ms");
    }

    /**
     * Sets the proper caching flags and redirects the response
     * @param response the response
     * @param url the url to redirect to
     * @param cache whether to cache the response or not
     */
    public void redirect(HttpServletResponse response, String url, boolean cache) throws IOException {
        if (cache) {
            WebUtils.cache(response, 60 * 60); // One hour
        } else {
            WebUtils.nocache(response);
        }
        response.sendRedirect(url);
    }

    /**
     * Process image
     * @param z the zoom level
     * @param bounds the tile bounds
     * @param mercator the mercator calculator
     * @param locations the locations
     * @return the resulting image
     */
    private BufferedImage processImage(int z, double[] bounds, GlobalMercator mercator, java.util.List<LocationVo> locations) {

        BufferedImage image = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        GraphicsUtils.antialias(g2);

        Color col = new Color(143, 47, 123);
        Color fillCol = new Color(173, 87, 161, 80);

        int xy0[] =  mercator.LatLonToPixels(-bounds[0], bounds[1], z);

        locations.stream().forEach(location -> {

            if ("POINT".equals(location.getType())) {
                PointVo pt = location.getPoints().get(0);

                int xy[] = mercator.LatLonToPixels(pt.getLat(), pt.getLon(), z);
                double px = xy[0] - xy0[0];
                double py = -(xy[1] - xy0[1]);
                double radius = (z < 6) ? 1.0 : 2.0;

                Shape theCircle = new Ellipse2D.Double(px - radius, py - radius, 2.0 * radius, 2.0 * radius);
                g2.setColor(col);
                g2.fill(theCircle);

            } else if ("POLYLINE".equals(location.getType()) || "POLYGON".equals(location.getType())) {
                GeneralPath path = new GeneralPath();
                for (int i = 0; i < location.getPoints().size(); i++) {
                    PointVo pt = location.getPoints().get(i);
                    int xy[] = mercator.LatLonToPixels(pt.getLat(), pt.getLon(), z);
                    double px = xy[0] - xy0[0];
                    double py = -(xy[1] - xy0[1]);

                    if (i == 0) {
                        path.moveTo(px, py);
                    } else {
                        path.lineTo(px, py);
                    }
                }
                if ("POLYGON".equals(location.getType())) {
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
        return image;
    }

    /**
     * Searches for locations
     * @param bounds bounds of the tile
     * @return the locations of the tile
     */
    private java.util.List<LocationVo> searchLocations(double[] bounds) {

        Location loc = new Location();
        loc.setType(Location.LocationType.POLYGON);
        loc.getPoints().add(new Point(-bounds[0], bounds[1]));
        loc.getPoints().add(new Point(-bounds[2], bounds[1]));
        loc.getPoints().add(new Point(-bounds[2], bounds[3]));
        loc.getPoints().add(new Point(-bounds[0], bounds[3]));

        MessageSearchParams params = new MessageSearchParams();
        params.setMaxHits(10000);
        params.getLocations().add(loc);

        return searchLocations(params);
    }

    /**
     * Searches for locations with the given search parameters
     */
    public java.util.List<LocationVo> searchLocations(MessageSearchParams param) {
        java.util.List<LocationVo> result = new ArrayList<>();

        try {
            MessageSearchResult messages = messageSearchService.search(param);

            for (MessageVo msg : messages.getMessages()) {
                msg.getLocations().stream()
                        .filter(loc -> loc.getPoints().size() > 0)
                        .forEach(result::add);
            }
            return result;

        } catch (Exception e) {
            log.error("Error performing search : " + e, e);
            return result;
        }
    }

}

