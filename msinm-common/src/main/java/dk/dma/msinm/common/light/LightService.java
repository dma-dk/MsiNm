package dk.dma.msinm.common.light;

import dk.dma.msinm.common.templates.TemplateContext;
import dk.dma.msinm.common.templates.TemplateService;
import dk.dma.msinm.common.templates.TemplateType;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a light characteristic string into a LightModel and
 * formats the LightModel into a human readable text for a specific language.
 */
public class LightService implements LightConstants {

    private static final Pattern ELEVATION = Pattern.compile("^(\\d+)m.*");
    private static final Pattern PERIOD = Pattern.compile("^(\\d+)s.*");
    private static final Pattern RANGE = Pattern.compile("^(\\d+)M.*");

    @Inject
    private TemplateService templateService;

    /**
     * Parses the light characteristic string into a light model
     * @param light the light characteristic
     * @return the light model
     */
    public LightModel parse(String light) {
        if (StringUtils.isBlank(light)) {
            return null;
        }

        // Normalize the light spec.
        light = light.replace("Qk", "Q");
        light = light.replace("Bu", "B");
        light = light.replace("Bl", "B");
        light = light.replace("Occ", "Oc");
        light = light.replace("F.", "F");
        light = light.replace("V.", "V");
        light = light.replace("Q.", "Q");
        light = light.replace("U.", "U");
        light = light.replace("I.", "I");
        light = light.replace("Gp", "");
        light = light.replace(".", "");
        light = light.replaceAll("\\s+"," ");

        LightModel lightModel = new LightModel();

        while (StringUtils.isNotBlank(light)) {
            light = light.trim();
            boolean match = false;

            // Match phases
            for (Phase p : Phase.values()) {
                if (light.startsWith(p.name())) {
                    match = true;
                    LightModel.Light l = lightModel.newLight();
                    l.phase = p;
                    light = light.substring(p.name().length()).trim();
                    if (light.startsWith("(")) {
                        String grp = light.substring(1, light.indexOf(")"));
                        light = light.substring(grp.length() + 2).trim();
                        if (p == Phase.Mo) {
                            l.morseCode = getTelephonyCode(grp.charAt(0)).toUpperCase();
                        } else {
                            l.grouped = true;
                            Arrays.asList(grp.split("\\+")).forEach(i -> l.groupSpec.add(Integer.valueOf(i.trim())));
                        }
                    }
                    break;
                }
            }
            if (match) {
                continue;
            }

            // "+" indicates new light group
            if (light.startsWith("+")) {
                light = light.substring(1).trim();
                continue;
            }

            // Match colors
            for (Color c : Color.values()) {
                if (light.startsWith(c.name())) {
                    match = true;
                    lightModel.getLight().colors.add(c);
                    light = light.substring(c.name().length()).trim();
                    break;
                }
            }
            if (match) {
                continue;
            }

            // Extract elevation
            Matcher m = ELEVATION.matcher(light);
            if (m.find()) {
                String elev = m.group(1);
                lightModel.elevation = Integer.valueOf(elev);
                light = light.substring(elev.length() + 1).trim();
                continue;
            }

            // Extract period
            m = PERIOD.matcher(light);
            if (m.find()) {
                String period = m.group(1);
                lightModel.period = Integer.valueOf(period);
                light = light.substring(period.length() + 1).trim();
                continue;
            }

            // Extract range
            m = RANGE.matcher(light);
            if (m.find()) {
                String range = m.group(1);
                lightModel.range = Integer.valueOf(range);
                light = light.substring(range.length() + 1).trim();
                continue;
            }

            // Give up
            break;
        }

        return lightModel;
    }

    /**
     * Parses the light characteristic string into a human readable version
     * @param language the language of choice
     * @param light the light characteristic
     * @return the human readable version
     */
    public String parse(String language, String light) throws Exception {
        LightModel lightModel = parse(light);

        Map<String, Object> data = new HashMap<>();
        data.put("lightModel", lightModel);

        TemplateContext ctx = templateService.getTemplateContext(
                TemplateType.Light,
                "light-characteristics.ftl",
                data,
                language,
                null);

        String text = templateService.process(ctx);
        text = text.replaceAll("\\s+"," ");
        text = text.replaceAll(" ;",";");
        text = text.replaceAll(" ,",",");
        text = text.replaceAll("- ","-");
        return text;
    }

}
