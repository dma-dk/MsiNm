package dk.dma.msinm.templates;

import dk.dma.msinm.common.util.PositionFormat;
import freemarker.core.Environment;
import freemarker.template.*;

import java.io.IOException;
import java.util.Map;

/**
 * This Freemarker directive will format latitude and/or longtitude
 */
public class LatLonDirective implements TemplateDirectiveModel {

    private static final String PARAM_LAT = "lat";
    private static final String PARAM_LON = "lon";

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
            Double lon = (latModel == null) ? null : lonModel.getAsNumber().doubleValue();

            if (lat != null) {
                env.getOut().write(PositionFormat.formatLat(env.getLocale(), lat));
            }
            if (lon != null) {
                if (lat != null) {
                    env.getOut().write("  ");
                }
                env.getOut().write(PositionFormat.formatLon(env.getLocale(), lon));
            }
        } catch (Exception e) {
            // Prefer robustness over correctness
        }
    }
}
