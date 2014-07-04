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

/**
 * Represents a time translator that can translate to and fro an english version of the time description
 */
public class TimeTranslator implements TimeConstants {

    private static final Map<String, TimeTranslator> CACHE = new HashMap<>();

    String[] monthsEn = MONTHS_EN.split(",");
    String[] seasonsEn = SEASONS_EN.split(",");
    String[] months;
    String[] seasons;
    Map<String, String> translateToEnRules      = new LinkedHashMap<>();
    Map<String, String> translateFromEnRules    = new LinkedHashMap<>();

    /**
     * Disable public construction
     */
    private TimeTranslator(String language) throws TimeException {

        String file = "/timeTranslator_" + language + ".txt";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(file), "UTF-8"))) {

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

                    case "translate_rule":
                        if (value.contains("<->")) {
                            int x = value.indexOf("<->");
                            String local = unquote(value.substring(0, x).trim());
                            String english = unquote(value.substring(x + "<->".length()).trim());
                            translateToEnRules.put(local, english);
                            translateFromEnRules.put(english, local);
                        } else if (value.contains("->")) {
                            int x = value.indexOf("->");
                            String local = unquote(value.substring(0, x).trim());
                            String english = unquote(value.substring(x + "->".length()).trim());
                            translateToEnRules.put(local, english);
                        } else if (value.contains("<-")) {
                            int x = value.indexOf("<-");
                            String local = unquote(value.substring(0, x).trim());
                            String english = unquote(value.substring(x + "<-".length()).trim());
                            translateFromEnRules.put(english, local);
                        } else {
                            throw new TimeException("Invalid rewrite rule: " + value);
                        }
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

        // Add the month translation rules
        for (int m = 0; m < 12; m++) {
            translateToEnRules.put(months[m], monthsEn[m]);
            translateFromEnRules.put(monthsEn[m], months[m]);
        }

        // Check that seasons is defined
        if (seasons == null || seasons.length != 4) {
            throw new TimeException("Seasons not properly defined in " + file);
        }

        // Add the month translation rules
        for (int m = 0; m < 4; m++) {
            translateToEnRules.put(seasons[m], seasonsEn[m]);
            translateFromEnRules.put(seasonsEn[m], seasons[m]);
        }
    }

    /**
     * Return the time translator for the specific language
     * @param language the language
     * @return the time translator for the specific language
     */
    public synchronized static TimeTranslator get(String language) throws TimeException {

        // Cache the parser
        TimeTranslator parser = CACHE.get(language);
        if (parser == null) {
            parser = new TimeTranslator(language);
            CACHE.put(language, parser);
        }

        return parser;
    }

    /**
     * Translate the time description
     * @param time the time description to translate
     * @param translateRules the translation rules to apply
     * @return the result
     */
    public String translate(String time, Map<String, String> translateRules) throws TimeException {

        BufferedReader reader = new BufferedReader(new StringReader(time));
        String line;
        StringBuilder result = new StringBuilder();
        try {
            while((line = reader.readLine()) != null) {
                //line = line.trim().toLowerCase();

                // Replace according to replace rules
                for (String key : translateRules.keySet()) {
                    String value = translateRules.get(key);

                    Matcher m = Pattern.compile(key, Pattern.CASE_INSENSITIVE).matcher(line);
                    StringBuffer sb = new StringBuffer();
                    while (m.find()) {
                        String text = m.group();
                        m.appendReplacement(sb, value);
                    }
                    m.appendTail(sb);
                    line = sb.toString();
                }

                // Capitalize, unless the line starts with something like "a) xxx"
                if (!line.matches("\\w\\) .*")) {
                    line = StringUtils.capitalize(line);
                }

                result.append(line + "\n");
            }
        } catch (Exception e) {
            throw new TimeException("Failed translating time description", e);
        }

        return result.toString().trim();
    }

    /**
     * Translate the given time description to English
     * @param time the time description to translate
     * @return the translated time description
     */
    public String translateToEnglish(String time) throws TimeException {
        return translate(time, translateToEnRules);
    }

    /**
     * Translate the given time description from English
     * @param time the time description to translate
     * @return the translated time description
     */
    public String translateFromEnglish(String time) throws TimeException {
        return translate(time, translateFromEnRules);
    }

    public static void main(String... args) throws TimeException {

        TimeTranslator translator = TimeTranslator.get("da");

        System.out.println(translator.translateToEnglish("a) Until 11 October 2014.\nb) 23 May - 7 June 2014, hours 0500 - 2200."));
    }

}
