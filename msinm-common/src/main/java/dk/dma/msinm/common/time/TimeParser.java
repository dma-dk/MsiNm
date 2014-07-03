package dk.dma.msinm.common.time;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Represents a time parser for a specific language
 */
public class TimeParser {

    private static final Map<String, TimeParser> CACHE = new HashMap<>();

    String[] months;
    String[] seasons;
    Map<String, String> rewriteRules = new LinkedHashMap<>();

    /**
     * Disable public construction
     */
    private TimeParser(String language) throws TimeException {


        // Default rewrite rules
        rewriteRules.put("\\w\\) ", "");
        rewriteRules.put("\\.", "");
        rewriteRules.put("\\,", "");
        rewriteRules.put("\\s+", " ");

        String file = "/timeParser_" + language + ".txt";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(file)))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (StringUtils.isBlank(line) || line.startsWith("#")) {
                    continue;
                }

                int index = line.indexOf(":");
                if (index == -1) {
                    throw new TimeException("Invalid record: " + line);
                }
                String key = line.substring(0, index).trim();
                String value = line.substring(index + 1).trim();

                switch (key) {
                    case "months":
                        months = value.toLowerCase().split(",");
                        if (months.length != 12) {
                            throw new TimeException("Invalid month definition: " + value);
                        }
                        break;

                    case "seasons":
                        seasons = value.toLowerCase().split(",");
                        if (seasons.length != 4) {
                            throw new TimeException("Invalid season definition: " + value);
                        }
                        break;

                    case "rewrite_rule":
                        int x = value.indexOf("->");
                        if (x == -1) {
                            throw new TimeException("Invalid rewrite rule: " + value);
                        }
                        String from = unquote(value.substring(0, x).trim());
                        String to = unquote(value.substring(x + "->".length()).trim());
                        rewriteRules.put(from, to);
                        break;

                    default:
                        throw new TimeException("Invalid record: " + line);
                }

            }

        } catch (IOException e) {
            throw new TimeException("Error reading " + file + ": " + e);
        }

        // Read the "months" property, or fall back to use the SimpleDateFormat definitions
        if (months == null) {
            months = new String[12];
            SimpleDateFormat dateFormat = new SimpleDateFormat( "MMMM", new Locale(language));
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            for (int m = 0; m < 12; m++) {
                cal.set(Calendar.MONTH, m);
                months[m] = dateFormat.format(cal.getTime());
            }
        }

        // Work in lowercase
        for (int m = 0; m < 12; m++) {
            months[m] = months[m].trim().toLowerCase();
        }

        // Check that seasons is defined
        if (seasons == null) {
            throw new TimeException("Seasons not defined in " + file);
        }

    }

    /**
     * Removes start-end quotes
     * @param value the value to remove quotes from
     * @return the unquoted value
     */
    String unquote(String value) {
        if (value != null && value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }

    public String parse(String time, Date now) throws IOException {
        String monthMatch = "(" + Arrays.asList(months).stream().collect(Collectors.joining("|")) + ")";
        String seasonMatch = "(" + Arrays.asList(seasons).stream().collect(Collectors.joining("|")) + ")";
        String dayMatch = "(\\\\d{1,2})";
        String yearMatch = "(\\\\d{4})";
        String hourMatch = "(\\\\d{4})";
        String weekMatch = "(\\\\d{1,2})";

        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM");
        SimpleDateFormat dateFormat = new SimpleDateFormat("d");

        BufferedReader reader = new BufferedReader(new StringReader(time));
        String line;
        StringBuilder result = new StringBuilder();
        while((line = reader.readLine()) != null) {
            line = line.trim().toLowerCase();

            // Replace according to replace rules
            for (String key : rewriteRules.keySet()) {
                String value = rewriteRules.get(key);

                value = value.replaceAll("\\$current_year", yearFormat.format(now));
                value = value.replaceAll("\\$current_month", monthFormat.format(now));
                value = value.replaceAll("\\$current_date", dateFormat.format(now));

                String from = key;
                from = from.replaceAll("\\$month", monthMatch);
                from = from.replaceAll("\\$season", seasonMatch);
                from = from.replaceAll("\\$date", dayMatch);
                from = from.replaceAll("\\$year", yearMatch);
                from = from.replaceAll("\\$hour", hourMatch);
                from = from.replaceAll("\\$week", weekMatch);

                Matcher m = Pattern.compile(from).matcher(line);
                StringBuffer sb = new StringBuffer();
                while (m.find()) {
                    String text = m.group();
                    m.appendReplacement(sb, value);
                }
                m.appendTail(sb);
                line = sb.toString();
            }
            result.append(line + "\n");
        }
        return result.toString();
    }



    /**
     * Return the time parser for the specific language
     * @param language the language
     * @return the time parser for the specific language
     */
    public synchronized static TimeParser get(String language) throws TimeException {

        // Cache the parser
        TimeParser parser = CACHE.get(language);
        if (parser == null) {
            parser = new TimeParser(language);
            CACHE.put(language, parser);
        }

        return parser;
    }


    public static void main(String... args) throws TimeException, IOException {
        Date now = new Date();
        TimeParser parser = TimeParser.get("en");

        System.out.println(parser.parse("Mid-July - end October 2014.", now));
        System.out.println(parser.parse("9. - 15. january 2014", now));

    }

}
