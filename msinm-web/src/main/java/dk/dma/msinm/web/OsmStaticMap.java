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
import dk.dma.msinm.common.util.WebUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Currently MSI-NM use http://staticmap.openstreetmap.de/staticmap.php to generate static maps
 * via the {@code AbstractImageServlet} servlet.<br/>
 * However, this service is quite often down.
 * <p/>
 * The {@code OsmStaticMap} servlet is an attempt to re-implement the service in java.
 * It is based on the staticMapLite project ported from PHP to java, see:
 * http://sourceforge.net/p/staticmaplite/code/HEAD/tree/staticmap.php
 *
 * TODO: Fix rounding errors. Seems to be downloading too many tiles - try:
 * http://localhost:8080/static-map-lite?center=56,12&zoom=10&size=256x256
 *
 * TODO: Need to download tiles in parallel to speed it up
 * TODO: Need to clean up the tile and map caches on regular intervals
 */
@WebServlet(value = "/static-map-lite", asyncSupported = true)
public class OsmStaticMap extends HttpServlet {

    public static final String OSM_URL = "http://tile.openstreetmap.org/%d/%d/%d.png";
    public static final int RESPONSE_CACHE_TIME_SEC = 60 * 60 * 24 * 14;

    protected static final boolean useTileCache = true;
    protected static final String tileCacheBaseDir = "osm_cache/tiles";

    protected static final boolean useMapCache = true;
    protected static final String mapCacheBaseDir = "osm_cache/maps";

    @Inject
    Logger log;

    @Inject
    RepositoryService repositoryService;

    /**
     * Main GET method
     *
     * @param request  servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Sanity checks
        if (StringUtils.isBlank(request.getParameter("zoom")) || StringUtils.isBlank(request.getParameter("center")) || StringUtils.isBlank(request.getParameter("size"))) {
            throw new IllegalArgumentException("Must provide zoom, size and center parameters");
        }

        MapImageCtx ctx = new MapImageCtx();

        parseParams(ctx, request);
        if(useMapCache){
            // use map cache, so check cache for map
            if(!checkMapCache(ctx)){
                // map is not in cache, needs to be build
                BufferedImage image = makeMap(ctx);

                Path path = repositoryService.getRepoRoot().resolve(mapCacheIDToFilename(ctx));
                Files.createDirectories(path.getParent());
                ImageIO.write(image, "png", path.toFile());

                // And write to response
                sendHeader(response);
                ImageIO.write(image, "png", response.getOutputStream());

            } else {
                // map is in cache
                sendHeader(response);
                Path path = repositoryService.getRepoRoot().resolve(mapCacheIDToFilename(ctx));
                response.setContentLength((int) path.toFile().length());

                FileInputStream fileInputStream = new FileInputStream(path.toFile());
                OutputStream responseOutputStream = response.getOutputStream();
                int bytes;
                while ((bytes = fileInputStream.read()) != -1) {
                    responseOutputStream.write(bytes);
                }
            }

        } else {
            // no cache, make map, send headers and deliver png
            BufferedImage image = makeMap(ctx);
            sendHeader(response);
            ImageIO.write(image, "png", response.getOutputStream());
        }
    }

    public void parseParams(MapImageCtx ctx, HttpServletRequest request) {
        // Extract zoom parameter
        ctx.zoom = Integer.valueOf(request.getParameter("zoom"));
        if (ctx.zoom > 18) {
            ctx.zoom = 18;
        }

        // Extract lat-lon from the center parameter
        String[] latLon = request.getParameter("center").split(",");
        if (latLon.length != 2) {
            throw new IllegalArgumentException("Invalid center parameter");
        }
        ctx.lat = Double.valueOf(latLon[0]);
        ctx.lon = Double.valueOf(latLon[1]);

        // Extract width and height from the size parameter
        String[] size = request.getParameter("size").split("x");
        if (size.length != 2) {
            throw new IllegalArgumentException("Invalid size parameter");
        }
        ctx.width = Integer.valueOf(size[0]);
        ctx.height = Integer.valueOf(size[1]);

        log.info(String.format("Received request for OSM static image with zoom=%d, center=(%f, %f), width=%d and height=%d", ctx.zoom, ctx.lat, ctx.lon, ctx.width, ctx.height));
    }

    private double lonToTile(double lon, int zoom){
        return ((lon + 180.0) / 360.0) * Math.pow(2.0, zoom);
    }

    private double latToTile(double lat, int zoom){
        return (1.0 - Math.log(Math.tan(lat * Math.PI / 180.0) + 1.0 / Math.cos(lat * Math.PI / 180.0)) / Math.PI) / 2.0 * Math.pow(2.0, zoom);
    }

    public void initCoords(MapImageCtx ctx){
        ctx.centerX = lonToTile(ctx.lon, ctx.zoom);
        ctx.centerY = latToTile(ctx.lat, ctx.zoom);
        ctx.offsetX = Math.floor((Math.floor(ctx.centerX) - ctx.centerX) * ctx.tileSize);
        ctx.offsetY = Math.floor((Math.floor(ctx.centerY) - ctx.centerY) * ctx.tileSize);
    }

    public BufferedImage createBaseMap(MapImageCtx ctx){
        BufferedImage image = new BufferedImage(ctx.width, ctx.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        int startX = (int)Math.floor(ctx.centerX-(ctx.width/ctx.tileSize)/2.0);
        int startY = (int)Math.floor(ctx.centerY-(ctx.height/ctx.tileSize)/2.0);
        int endX = (int)Math.ceil(ctx.centerX+(ctx.width/ctx.tileSize)/2.0);
        int endY = (int)Math.ceil(ctx.centerY+(ctx.height/ctx.tileSize)/2.0);
        ctx.offsetX = -Math.floor((ctx.centerX - Math.floor(ctx.centerX)) * ctx.tileSize);
        ctx.offsetY = -Math.floor((ctx.centerY - Math.floor(ctx.centerY)) * ctx.tileSize);
        ctx.offsetX += Math.floor(ctx.width / 2.0);
        ctx.offsetY += Math.floor(ctx.height / 2.0);
        ctx.offsetX += Math.floor(startX - Math.floor(ctx.centerX))*ctx.tileSize;
        ctx.offsetY += Math.floor(startY - Math.floor(ctx.centerY))*ctx.tileSize;

        for(int x=startX; x<=endX; x++){
            for(int y=startY; y<=endY; y++) {
                String url = String.format(OSM_URL, ctx.zoom, x, y);
                log.info("Fetching " + url);
                try {
                    BufferedImage tileImage = fetchTile(url);
                    double destX = (x - startX) * ctx.tileSize + ctx.offsetX;
                    double destY = (y - startY) * ctx.tileSize + ctx.offsetY;
                    g2.drawImage(tileImage, (int) destX, (int) destY, ctx.tileSize, ctx.tileSize, null);
                    image.flush();
                } catch (Exception e) {
                    log.warn("Failed loading image " + url);
                }
            }
        }
        return image;
    }

    private String tileUrlToFilename(String url){
        return tileCacheBaseDir + "/" + url.replace("http://", "");
    }

    public BufferedImage checkTileCache(String url){
        String filename = tileUrlToFilename(url);
        Path path = repositoryService.getRepoRoot().resolve(filename);
        try {
            if(Files.exists(path)){
                return ImageIO.read(path.toFile());
            }
        } catch (IOException e) {
            log.warn("Failed to load tile cache file " + filename);
        }
        return null;
    }

    public boolean checkMapCache(MapImageCtx ctx){
        ctx.mapCacheID = DigestUtils.md5Hex(serializeParams(ctx));
        String filename = mapCacheIDToFilename(ctx);
        Path path = repositoryService.getRepoRoot().resolve(filename);
        return Files.exists(path);
    }

    public String serializeParams(MapImageCtx ctx){
        return Arrays.asList(ctx.zoom, ctx.lat, ctx.lon, ctx.width, ctx.height).stream().map(String::valueOf).collect(Collectors.joining("&"));
    }

    public String mapCacheIDToFilename(MapImageCtx ctx){
        if(ctx.mapCacheFile == null) {
            ctx.mapCacheFile = mapCacheBaseDir
                    + "/" + ctx.zoom
                    + "/cache_" + ctx.mapCacheID.substring(0,2)
                    + "/" + ctx.mapCacheID.substring(2,4)
                    + "/" + ctx.mapCacheID.substring(4);
        }
        return ctx.mapCacheFile + ".png";
    }

    public void writeTileToCache(String url, BufferedImage image){
        try {
            String filename = tileUrlToFilename(url);
            Path path = repositoryService.getRepoRoot().resolve(filename);
            Files.createDirectories(path.getParent());
            ImageIO.write(image, "png", path.toFile());
        } catch (IOException e) {
            log.warn("Failed saving cached tile for url " + url);
        }
    }

    public BufferedImage fetchTile(String url) throws Exception {
        BufferedImage image = null;
        if(useTileCache) {
            image = checkTileCache(url);
        }
        if (image == null) {
            image = ImageIO.read(new URL(url));
            if (useTileCache) {
                writeTileToCache(url, image);
            }
        }
        return image;
    }

    public void sendHeader(HttpServletResponse response) {
        WebUtils.cache(response, RESPONSE_CACHE_TIME_SEC);
        response.setContentType("image/png");
    }

    public BufferedImage makeMap(MapImageCtx ctx){
        initCoords(ctx);
        return createBaseMap(ctx);
    }

    public static class MapImageCtx {
        int zoom;
        double lat = 0.0, lon = 0.0;
        int width = 256, height = 256;
        double centerX, centerY;
        double offsetX, offsetY;
        int tileSize = 256;
        String mapCacheID, mapCacheFile;
    }
}