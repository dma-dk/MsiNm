package dk.dma.msinm.templates;

import dk.dma.msinm.common.util.PositionFormatter;
import dk.dma.msinm.common.util.PositionFormatter.Format;
import freemarker.core.Environment;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This Freemarker directive will format latitude and/or longtitude
 */
public class LatLonDirective implements TemplateDirectiveModel {

    private static final Map<Locale, Format> formatterCache = new ConcurrentHashMap<>();

    private static final String PARAM_LAT = "lat";
    private static final String PARAM_LON = "lon";
    private static final String PARAM_FORMAT = "format";

    /**
     * Construct a format for audio positions
     * @param locale the locale
     * @return the format
     */
    public synchronized Format getAudioFormat(Locale locale) {
        Format format = formatterCache.get(locale);
        if (format != null) {
            return format;
        }

        ResourceBundle bundle = ResourceBundle.getBundle("position-format-audio", locale);
        String deg = bundle.getString("deg");
        String min = bundle.getString("min");
        String ns = bundle.getString("north") + "," + bundle.getString("south");
        String ew = bundle.getString("east") + "," + bundle.getString("west");
        format = new Format(
                "DEG-F[%d] " + deg + " MIN[%.1f] " + min + " DIR[" + ns + "]",
                "DEG-F[%d] " + deg + " MIN[%.1f] " + min + " DIR[" + ew + "]");
        formatterCache.put(locale, format);
        return format;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Environment env,
                        Map params,
                        TemplateModel[] loopVars,
                        TemplateDirectiveBody body)
            throws TemplateException, IOException {

        SimpleNumber latModel = (SimpleNumber)params.get(PARAM_LAT);
        SimpleNumber lonModel = (SimpleNumber)params.get(PARAM_LON);
        if (latModel == null && lonModel == null) {
            throw new TemplateModelException("The 'lat' and/or 'lon' parameter must be specified");
        }

        try {
            String separator = "  ";
            Double lat = (latModel == null) ? null : latModel.getAsNumber().doubleValue();
            Double lon = (lonModel == null) ? null : lonModel.getAsNumber().doubleValue();

            Format format = PositionFormatter.LATLON_DEC;
            SimpleScalar formatModel = (SimpleScalar) params.get(PARAM_FORMAT);
            if (formatModel != null) {
                if ("dec".equalsIgnoreCase(formatModel.getAsString())) {
                    format = PositionFormatter.LATLON_DEC;
                } else if ("sec".equalsIgnoreCase(formatModel.getAsString())) {
                    format = PositionFormatter.LATLON_SEC;
                } else if ("navtex".equalsIgnoreCase(formatModel.getAsString())) {
                    format = PositionFormatter.LATLON_NAVTEX;
                } else if ("audio".equalsIgnoreCase(formatModel.getAsString())) {
                    format = getAudioFormat(env.getLocale());
                    separator = " - ";
                }
            }

            if (lat != null) {
                env.getOut().write(PositionFormatter.format(env.getLocale(), format.getLatFormat(), lat));
            }
            if (lon != null) {
                if (lat != null) {
                    env.getOut().write(separator);
                }
                env.getOut().write(PositionFormatter.format(env.getLocale(), format.getLonFormat(), lon));
            }
        } catch (Exception e) {
            // Prefer robustness over correctness
        }
    }
}
