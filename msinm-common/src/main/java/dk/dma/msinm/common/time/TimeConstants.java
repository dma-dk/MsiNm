package dk.dma.msinm.common.time;

/**
 * Common functionality for Time classes
 */
public interface TimeConstants {

    public static final String MONTHS_EN = "January,February,March,April,May,June,July,August,September,October,November,December";

    public static final String SEASONS_EN = "Spring,Summer,Autumn,Winter";

    /**
     * Removes start-end quotes
     * @param value the value to remove quotes from
     * @return the unquoted value
     */
    default String unquote(String value) {
        if (value != null && value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }


}
