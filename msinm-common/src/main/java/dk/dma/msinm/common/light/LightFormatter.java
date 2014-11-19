package dk.dma.msinm.common.light;

/**
 * Can be used in e.g. Freemarker templates to format light characteristics strings
 */
public class LightFormatter {

    LightService lightService;
    String language;

    /**
     * Constructor
     * @param lightService the light service
     * @param language the language
     */
    public LightFormatter(LightService lightService, String language) {
        this.lightService = lightService;
        this.language = language;
    }


    /**
     * Formats the light characteristic string as a human readable string
     * @param light the light characteristic string
     * @return the human readable string or the light characteristic string if it is un-parsable
     */
    public String format(String light) {
        try {
            return lightService.parse(language, light);
        } catch (Exception e) {
            return light;
        }
    }
}
