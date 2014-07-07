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

import dk.dma.msinm.common.util.GraphicsUtils;
import dk.dma.msinm.model.Location;
import dk.dma.msinm.model.Point;
import dk.dma.msinm.service.MessageSearchParams;
import dk.dma.msinm.service.MessageSearchService;
import dk.dma.msinm.vo.LocationVo;
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

        Location loc = new Location();
        loc.setType(Location.LocationType.POLYGON);
        loc.getPoints().add(new Point(-bounds[0], bounds[1]));
        loc.getPoints().add(new Point(-bounds[2], bounds[1]));
        loc.getPoints().add(new Point(-bounds[2], bounds[3]));
        loc.getPoints().add(new Point(-bounds[0], bounds[3]));

        MessageSearchParams params = new MessageSearchParams();
        params.setMaxHits(10000);
        params.getLocations().add(loc);

        Color col = new Color(143, 47, 123);
        Color fillCol = new Color(173, 87, 161, 80);

        int xy0[] =  mercator.LatLonToPixels(-bounds[0], bounds[1], z);
        java.util.List<LocationVo> locations = messageSearchService.searchLocations(params);

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
}

