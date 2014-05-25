package dk.dma.msinm.web.wms;

import dk.dma.msinm.common.settings.annotation.Setting;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.stream.Collectors;

/**
 * Proxy WMS data
 */
@WebServlet(value = "/wms/*", asyncSupported = true)
public class WmsProxyServlet extends HttpServlet {

    // The color we want transparent
    final static Color[] MASKED_COLORS = { Color.WHITE, new Color(221, 241, 239) };
    final static int COLOR_DIST = 20;

    @Inject
    Logger log;

    @Inject
    @Setting(value = "wmsProvider", defaultValue = "http://kortforsyningen.kms.dk/")
    String wmsProvider;

    /**
     * Main GET method
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String params = request.getParameterMap()
                .entrySet()
                .stream()
                .map(p -> String.format("%s=%s", p.getKey(), p.getValue()[0]))
                .collect(Collectors.joining("&"));

        String url = wmsProvider + "?" + params;
        log.trace("Loading image " + url);

        BufferedImage image = ImageIO.read(new URL(url));
        image = transformWhiteToTransparent(image);

        OutputStream out = response.getOutputStream();
        ImageIO.write(image, "png", out);
        image.flush();
        out.close();
    }

    /**
     * Masks out white colour
     * @param image the image to mask out
     * @return the resulting image
     */
    private BufferedImage transformWhiteToTransparent(BufferedImage image) {

        BufferedImage dest = image;
        if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
            dest = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = dest.createGraphics();
            g2.drawImage(image, 0, 0, null);
            g2.dispose();

            image.flush();
        }

        // Mask out the white pixels
        final int width = image.getWidth();
        int[] imgData = new int[width];

        for (int y = 0; y < dest.getHeight(); y++) {
            // fetch a line of data from each image
            dest.getRGB(0, y, width, 1, imgData, 0, 1);
            // apply the mask
            for (int x = 0; x < width; x++) {
                for (Color col : MASKED_COLORS) {
                    int colDist
                            = Math.abs(col.getRed() - (imgData[x] >> 16 & 0x000000FF))
                            + Math.abs(col.getGreen() - (imgData[x] >> 8 & 0x000000FF))
                            + Math.abs(col.getBlue() - (imgData[x] & 0x000000FF));
                    if (colDist <= COLOR_DIST) {
                        imgData[x] = 0x00FFFFFF & imgData[x];
                    }
                }
            }
            // replace the data
            dest.setRGB(0, y, width, 1, imgData, 0, 1);
        }
        return dest;
    }
}
