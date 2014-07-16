package dk.dma.msinm.common.util;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * Formats and parses (TBD) latitude/longitude positions
 */
public class PositionFormat {

    /**
     * Defines the latitude-longitude types
     */
    public enum Type {
        LATITUDE("N", "S", 2),
        LONGITUDE("E", "W", 3);

        String posSuffix, negSuffix;
        int degDigits;

        private Type(String posSuffix, String negSuffix, int degDigits) {
            this.posSuffix = posSuffix;
            this.negSuffix = negSuffix;
            this.degDigits = degDigits;
        }
    }

    /**
     * Defines the format - whether to print out seconds or minutes with decimals
     */
    public enum Format {
        SECONDS,
        DECIMALS
    }

    /**
     * Formats the latitude in the decimal format using the given locale
     * @param locale the locale
     * @param lat the latitude
     * @return the formatted latitude
     */
    public static String formatLat(Locale locale, double lat) {
        return formatDegrees(locale, Type.LATITUDE, Format.DECIMALS, lat);
    }

    /**
     * Formats the latitude in the seconds format
     * @param lat the latitude
     * @return the formatted latitude
     */
    public static String formatLat(double lat) {
        return formatDegrees(null, Type.LATITUDE, Format.SECONDS, lat);
    }

    /**
     * Formats the longitude in the decimal format using the given locale
     * @param locale the locale
     * @param lon the longitude
     * @return the formatted longitude
     */
    public static String formatLon(Locale locale, double lon) {
        return formatDegrees(locale, Type.LONGITUDE, Format.DECIMALS, lon);
    }

    /**
     * Formats the longitude in the seconds format
     * @param lon the longitude
     * @return the formatted longitude
     */
    public static String formatLon(double lon) {
        return formatDegrees(null, Type.LONGITUDE, Format.SECONDS, lon);
    }

    /**
     * Work-horse method. Formats the given latitude or longitude according to the parameters
     * @param locale the locale. Must be defined for the {@code Format.DECIMALS} format
     * @param type the type, latitude or longitude
     * @param format the format
     * @param value the value to format
     * @return the formatted value
     */
    public static String formatDegrees(Locale locale, Type type, Format format, double value) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(format);
        if (format == Format.DECIMALS) {
            Objects.requireNonNull(locale);
        }

        String suffix = (value >= 0) ? type.posSuffix : type.negSuffix;
        value = Math.abs(value);
        int degrees = (int)Math.floor(value);
        double fraction = value - degrees;

        if (format == Format.SECONDS) {
            int minutes = (int)Math.floor(fraction * 60.0);
            int seconds = (int)Math.floor(fraction * 3600.0 - minutes * 60.0);
            return String.format("%0" + type.degDigits + "d° %02d' %02d\"%s",
                    degrees,
                    (int)Math.floor(minutes),
                    seconds,
                    suffix);
        } else {
            DecimalFormat fmt = (DecimalFormat)DecimalFormat.getNumberInstance(locale);
            fmt.setMinimumIntegerDigits(2);
            fmt.setMinimumFractionDigits(3);
            fmt.setMaximumFractionDigits(3);
            return String.format("%0" + type.degDigits + "d° %s'%s",
                    degrees,
                    fmt.format(fraction * 60.0),
                    suffix);
        }
    }

    public static void main(String[] args) {
        System.out.println(formatDegrees(new Locale("da"), Type.LATITUDE, Format.DECIMALS, 55.0911));
    }

}
