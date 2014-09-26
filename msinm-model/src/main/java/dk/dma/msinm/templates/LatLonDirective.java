package dk.dma.msinm.templates;

import dk.dma.msinm.common.util.PositionFormatter;
import freemarker.core.Environment;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.io.IOException;
import java.util.Map;

/**
 * This Freemarker directive will format latitude and/or longtitude
 */
public class LatLonDirective implements TemplateDirectiveModel {

    private static final String PARAM_LAT = "lat";
    private static final String PARAM_LON = "lon";
    private static final String PARAM_FORMAT = "format";

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
            Double lat = (latModel == null) ? null : latModel.getAsNumber().doubleValue();
            Double lon = (lonModel == null) ? null : lonModel.getAsNumber().doubleValue();

            PositionFormatter.Format format = PositionFormatter.LATLON_DEC;
            SimpleScalar formatModel = (SimpleScalar)params.get(PARAM_FORMAT);
            if (formatModel != null) {
                if ("dec".equalsIgnoreCase(formatModel.getAsString())) {
                    format = PositionFormatter.LATLON_DEC;
                } else if ("sec".equalsIgnoreCase(formatModel.getAsString())) {
                    format = PositionFormatter.LATLON_SEC;
                } else if ("navtex".equalsIgnoreCase(formatModel.getAsString())) {
                    format = PositionFormatter.LATLON_NAVTEX;
                }
            }

            if (lat != null) {
                env.getOut().write(PositionFormatter.format(env.getLocale(), format.getLatFormat(), lat));
            }
            if (lon != null) {
                if (lat != null) {
                    env.getOut().write("  ");
                }
                env.getOut().write(PositionFormatter.format(env.getLocale(), format.getLonFormat(), lon));
            }
        } catch (Exception e) {
            // Prefer robustness over correctness
        }
    }
}
