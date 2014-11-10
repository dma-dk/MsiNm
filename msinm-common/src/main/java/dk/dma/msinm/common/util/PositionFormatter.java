package dk.dma.msinm.common.util;

import java.util.Locale;

/**
 * Utility class used for formatting positions according to a flexible format template.
 * <p>
 * The format template can contain the following type parts:
 * <ul>
 *     <li>DEG: The degrees of a position</li>
 *     <li>MIN: The minutes of a position</li>
 *     <li>SEC: The seconds of a position</li>
 *     <li>DIR: The direction of a position</li>
 * </ul>
 * The numeric types can be tagged with a "-F" suffix, meaning that a floor version
 * of the value will be used (i.e. rounded down numerically).
 * <p>
 * Additionally, each type can be suffixed with a format specification. For the
 * numeric type, the <i>printf</i> format is used and for the "dir" type, a comma-separated
 * pair is used for positive respectively negative values.
 * <p>
 * Examples:
 * <ul>
 *     <li>MIN: the minutes.</li>
 *     <li>MIN-F: the rounded down minutes.</li>
 *     <li>MIN-F[%02d]: the rounded down minutes prefixed with zeros to ensure two integer digits.</li>
 *     <li>MIN[%06.3f]: the minutes formatted to have two integer digits and three decimal digits.</li>
 *     <li>DIR[N,S]: the direction which emits "N" for positive values and "S" for negative values.</li>
 * </ul>
 *
 * <p>
 * Lat-lon pairs can be formatted using a "latLonFormat" template where all occurrences of "LAT" is replaced
 * with the formatted latitude and all occurrences of "LON" is replaced with the formatted longitude.
 *
 * <p>
 * The specified locale is used to define the decimal separator (period or comma).
 *
 */
public class PositionFormatter {

    public static final Format LATLON_SEC = new Format(
            "DEG-F[%02d]\u00B0 MIN-F[%02d]' SEC-F[%02d]\"DIR[N,S]",
            "DEG-F[%03d]\u00B0 MIN-F[%02d]' SEC-F[%02d]\"DIR[E,W]");

    public static final Format LATLON_DEC = new Format(
            "DEG-F[%02d]\u00B0 MIN[%06.3f]'DIR[N,S]",
            "DEG-F[%03d]\u00B0 MIN[%06.3f]'DIR[E,W]");

    public static final Format LATLON_NAVTEX = new Format(
            "DEG-F[%02d]-MIN[%05.2f]DIR[N,S]",
            "DEG-F[%03d]-MIN[%05.2f]DIR[E,W]");


    /**
     * Formats the position.
     *
     * @param lat the latitude
     * @param lon the longitude
     * @return the formatted value
     */
    public static String format(double lat, double lon) {
        return format(null, null, null, lat, lon);
    }

    /**
     * Formats the position according to the give format.
     *
     * @param format the position format
     * @param lat the latitude
     * @param lon the longitude
     * @return the formatted value
     */
    public static String format(Format format, double lat, double lon) {
        return format(null, format, null, lat, lon);
    }

    /**
     * Formats the position according to the give format using the default locale.
     *
     * The latLonFormat is used as a template and all occurrences of "LAT" is replaced
     * with the latitude and all occurrences of "LON" is replaced with the longitude.
     *
     * @param format the position format
     * @param latLonFormat the combined lat lon template
     * @param lat the latitude
     * @param lon the longitude
     * @return the formatted value
     */
    public static String format(Format format, String latLonFormat, double lat, double lon) {
        return format(null, format, latLonFormat, lat, lon);
    }

    /**
     * Formats the position according to the give format using the given locale.
     *
     * @param locale the locale
     * @param format the position format
     * @param lat the latitude
     * @param lon the longitude
     * @return the formatted value
     */
    public static String format(Locale locale, Format format, double lat, double lon) {
        return format(locale, format, null, lat, lon);
    }

    /**
     * Formats the position according to the give format using the given locale.
     *
     * The latLonFormat is used as a template and all occurrences of "LAT" is replaced
     * with the latitude and all occurrences of "LON" is replaced with the longitude.
     *
     * @param locale the locale
     * @param format the position format
     * @param latLonFormat the combined lat lon template
     * @param lat the latitude
     * @param lon the longitude
     * @return the formatted value
     */
    public static String format(Locale locale, Format format, String latLonFormat, double lat, double lon) {
        // Set defaults
        format = (format == null) ? LATLON_SEC : format;
        latLonFormat = (latLonFormat == null) ? "LAT LON" : latLonFormat;

        String latitude = format(locale, format.getLatFormat(), lat);
        String longitude = format(locale, format.getLonFormat(), lon);

        return latLonFormat.replaceAll("LAT", latitude).replaceAll("LON", longitude);
    }

    /**
     * Formats the value according to the give format using the default locale
     * @param format the format
     * @param value the value to format
     * @return the formatted value
     */
    public static String format(String format, double value) {
        return format(null, format, value);
    }

    /**
     * Formats the value according to the give format using the given locale
     * @param locale the locale
     * @param format the format
     * @param value the value to format
     * @return the formatted value
     */
    public static String format(Locale locale, String format, double value) {
        locale = (locale != null) ? locale : Locale.ENGLISH;

        double val = Math.abs(value);
        val = Math.abs(val);
        int degrees = (int)Math.floor(val);
        double fraction = val - degrees;
        int minutes = (int)Math.floor(fraction * 60.0);
        int seconds = (int)Math.floor(fraction * 3600.0 - minutes * 60.0);


        StringBuilder result = new StringBuilder();
        Part p;
        while ((p = findPart(format, "DEG")) != null) {
            format = p.floor
                    ? p.replaceNumber(locale, degrees, "%02d")
                    : p.replaceNumber(locale, val, "%.2f");
        }
        while ((p = findPart(format, "MIN")) != null) {
            format = p.floor
                    ? p.replaceNumber(locale, minutes, "%02d")
                    : p.replaceNumber(locale, fraction * 60.0, "%02f");
        }
        while ((p = findPart(format, "SEC")) != null) {
            format = p.floor
                    ? p.replaceNumber(locale, seconds, "%02d")
                    : p.replaceNumber(locale, fraction * 3600.0 - minutes * 60.0, "%02f");
        }
        while ((p = findPart(format, "DIR")) != null) {
            int signIndex = value >= 0.0 ? 0 : 1;
            format = p.replaceIndex(signIndex);
        }
        return format;

    }

    /**
     * Looks for a format part of the given type and parses it into a Part object.
     * Returns null if the part type is not found
     * @param format the position format to search
     * @param type the part type to search for
     * @return the Part object or null
     */
    private static Part findPart(String format, String type) {
        int index = format.indexOf(type);
        if (index == -1) {
            return null;
        }

        Part p = new Part();
        p.type = type;
        p.before = format.substring(0, index);

        // Look for a floor postfix
        if (format.indexOf(type + "-F") == index) {
            p.floor = true;
            type = type + "-F";
        }

        // Check if the format defines a "[%02d]" style format
        if (format.indexOf(type + "[") == index && format.indexOf("]", index) > -1) {
            p.format = format.substring(index + type.length() + 1, format.indexOf("]", index));
            p.after = format.substring(format.indexOf("]", index) + 1);
        } else {
            p.after = format.substring(index + type.length());
        }
        return p;
    }


    public static void main(String[] args) {
        System.out.println(PositionFormatter.format(Locale.ENGLISH, "DEG[%02d], MIN[%2d], MIN[%.2f], SEC[%02d], DIR[N,S]", 55.0999));
        System.out.println(PositionFormatter.format(Locale.ENGLISH, LATLON_DEC.getLatFormat(), 55.2211));
        System.out.println(PositionFormatter.format(Locale.ENGLISH, LATLON_NAVTEX.getLatFormat(), 55.512345));
    }

    /**
     * Defines a combined lat-lon format
     */
    public static class Format {
        String latFormat, lonFormat;

        public Format(String latFormat, String lonFormat) {
            this.latFormat = latFormat;
            this.lonFormat = lonFormat;
        }

        public String getLatFormat() {
            return latFormat;
        }

        public String getLonFormat() {
            return lonFormat;
        }
    }

    /**
     * Helper class
     * Encapsulates a part of a position format such as ""
     * The type field can be one of "DEG", "MIN", "SEC" or "DIR"
     * The format can be a printf format for the numeric types (e.g. "%02d" or "%.2f") or a comma-separated array of values for "DIR".
     * The "before" and "after" fields contain the part of the format before and after the matched type part
     */
    static class Part {
        String type;
        String format;
        String before;
        String after;
        boolean floor;

        /**
         * Replaces the matched type part with value formatted according to the current format
         * @param locale the locale
         * @param value the value
         * @param defaultFormat if format to use, if the format field is undefined
         * @return the concatenated before - formatted value - after string.
         */
        public String replaceNumber(Locale locale, Object value, String defaultFormat) {
            String fmt = (format == null) ? defaultFormat : format;
            String val;
            if (value == null || !(value instanceof Number)) {
                val = "";
            } else {
                if (fmt.endsWith("d")) {
                    value = ((Number) value).intValue();
                }
                val = String.format(locale, fmt, value);
            }
            return before + val + after;
        }

        /**
         * Replaces the matched type part with the indexed value of the the format
         * @param signIndex the value
         * @return the concatenated before - formatted value - after string.
         */
        public String replaceIndex(int signIndex) {
            String val;
            if (format == null || signIndex >= format.split(",").length) {
                val = "";
            } else {
                val = format.split(",")[signIndex];
            }
            return before + val + after;
        }
    }
}
