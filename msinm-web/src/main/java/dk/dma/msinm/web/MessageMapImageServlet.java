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
import dk.dma.msinm.model.Location;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.service.MessageService;
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
 * Returns and caches a thumbnail image for a message.
 * <p></p>
 * Can be used e.g. for a grid layout in search results.
 */
@WebServlet(value = "/message-map-image/*", asyncSupported = true)
public class MessageMapImageServlet extends AbstractMapImageServlet  {

    private static Image msiImage, nmImage;

    @Inject
    Logger log;

    @Inject
    MessageService messageService;

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
                    imageFileExists = createMapImage(
                            locations,
                            imageRepoPath,
                            getMessageImage(message),
                            message.getUpdated());
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
            String imageUrl = app.getBaseUri() + "/img/msi.png";
            try {
                msiImage = ImageIO.read(new URL(imageUrl));
            } catch (IOException e) {
                log.error("This should never happen - could not load image from " + imageUrl);
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
            String imageUrl = app.getBaseUri() + "/img/nm.png";
            try {
                nmImage = ImageIO.read(new URL(imageUrl));
            } catch (IOException e) {
                log.error("This should never happen - could not load image from " + imageUrl);
            }
        }
        return nmImage;
    }
}
