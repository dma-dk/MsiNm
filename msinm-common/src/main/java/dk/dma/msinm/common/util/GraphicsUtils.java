package dk.dma.msinm.common.util;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Graphics-related utility functions
 */
public class GraphicsUtils {

    private static RenderingHints antialiasRenderHints;
    static {
        Map<RenderingHints.Key, Object> map = new HashMap<RenderingHints.Key, Object>();
        map.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        map.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        map.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        map.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        map.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        antialiasRenderHints = new RenderingHints(map);
    }

    private GraphicsUtils() {
    }

    /**
     * Sets rendering hints of the graphical context to turn on anti-aliasing
     * @param g2 the graphical context
     */
    public static void antialias(Graphics2D g2) {
        g2.setRenderingHints(antialiasRenderHints);
    }
}
