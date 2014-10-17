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

import dk.dma.msinm.common.MsiNmApp;
import dk.dma.msinm.common.model.DataFilter;
import dk.dma.msinm.model.Location;
import dk.dma.msinm.reporting.Report;
import dk.dma.msinm.reporting.ReportService;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Returns and caches a thumbnail image for a report.
 * <p></p>
 * Can be used e.g. for a grid layout in search results.
 */
@WebServlet(value = "/report-map-image/*", asyncSupported = true)
public class ReportMapImageServlet extends AbstractMapImageServlet  {

    private static Image reportImage;

    @Inject
    Logger log;

    @Inject
    ReportService reportService;

    @Inject
    MsiNmApp app;

    /**
     * Main GET method
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // Strip png path of the request path info to get the id of the report
            int id = Integer.valueOf(request.getPathInfo().substring(1).split("\\.")[0]);

            // Look up the report
            Report report = reportService.fetchReportById(id, DataFilter.get(DataFilter.LOCATIONS));
            if (report == null) {
                throw new IllegalArgumentException("Report " + id + " does not exist");
            }

            List<Location> locations = getReportLocations(report);
            if (locations.size() > 0) {
                // Construct the image file name for the report
                String imageName = String.format("map_%d.png", mapImageSize);

                // Create a hashed sub-folder for the image file
                Path imageRepoPath = reportService.getReportRepoFolder(report).resolve(imageName);

                // If the image file does not exist
                // generate a new image file
                boolean imageFileExists = Files.exists(imageRepoPath);
                if (!imageFileExists ||
                        report.getCreated().getTime() > Files.getLastModifiedTime(imageRepoPath).toMillis()) {
                    imageFileExists = createMapImage(
                            locations,
                            imageRepoPath,
                            getReportImage(),
                            report.getCreated());
                }

                // Either return the image file, or a place holder image
                if (imageFileExists) {
                    // Redirect the the repository streaming service
                    String uri = reportService.getReportFileRepoUri(report, imageName);
                    response.sendRedirect(uri);
                    return;
                }
            }

        } catch (Exception ex) {
            log.warn("Error fetching map image for report: " + ex);
        }

        // Show a placeholder image
        response.sendRedirect(IMAGE_PLACEHOLDER);
    }

    /**
     * Extracts the locations from the report
     * @param report the report
     * @return the list of locations
     */
    public List<Location> getReportLocations(Report report) {
        List<Location> result = new ArrayList<>();
        if (report != null) {
            result.addAll(report.getLocations()
                    .stream()
                    .filter(location -> location.getPoints().size() > 0)
                    .collect(Collectors.toList()));
        }
        return result;
    }

    /**
     * Returns the report symbol image
     * @return the report symbol image
     */
    private synchronized Image getReportImage() {
        if (reportImage == null) {
            String imageUrl = app.getBaseUri() + "/img/msi.png";
            try {
                reportImage = ImageIO.read(new URL(imageUrl));
            } catch (IOException e) {
                log.error("This should never happen - could not load image from " + imageUrl);
            }
        }
        return reportImage;
    }
}
