package dk.dma.msinm.common.time;

import org.apache.commons.lang.StringUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Represents a time parser for the english version of the time description
 */
public class TimeParser implements TimeConstants {

    private static TimeParser parser;

    String[] months = MONTHS_EN.toLowerCase().split(",");
    String[] seasons = SEASONS_EN.toLowerCase().split(",");
    Map<String, String> rewriteRules = new LinkedHashMap<>();

    /**
     * Disable public construction
     */
    private TimeParser() throws TimeException {

        // Default rewrite rules
        rewriteRules.put("\\w\\) ", "");
        rewriteRules.put("\\.", "");
        rewriteRules.put("\\,", "");
        rewriteRules.put("\\s+", " ");

        String file = "/timeParser.txt";
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
    }

    /**
     * Return the time parser
     * @return the time parser
     */
    public synchronized static TimeParser get() throws TimeException {

        // Cache the parser
        if (parser == null) {
            parser = new TimeParser();
        }

        return parser;
    }

    /**
     * Parse the time description into its XML representation
     * @param time the time description to parse
     * @return the result
     */
    public String parse(String time) throws TimeException {
        String monthMatch = "(" + Arrays.asList(months).stream().collect(Collectors.joining("|")) + ")";
        String seasonMatch = "(" + Arrays.asList(seasons).stream().collect(Collectors.joining("|")) + ")";
        String dayMatch = "(\\\\d{1,2})";
        String yearMatch = "(\\\\d{4})";
        String hourMatch = "(\\\\d{4})";
        String weekMatch = "(\\\\d{1,2})";

        BufferedReader reader = new BufferedReader(new StringReader(time));
        String line;
        StringBuilder result = new StringBuilder();
        try {
            while((line = reader.readLine()) != null) {
                line = line.trim().toLowerCase();

                // Replace according to replace rules
                for (String key : rewriteRules.keySet()) {
                    String value = rewriteRules.get(key);

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
        } catch (Exception e) {
            throw new TimeException("Failed converting time description into XML", e);
        }
        return "<time-result>" + result.toString() + "</time-result>";
    }

    /**
     * Parses the time into a {@code TimeModel} model.
     * @param time the time description to parse
     * @return the time model
     */
    public TimeModel parseModel(String time) throws TimeException {
        String timeXml = null;
        try {
            // Transform the time description into xml
            timeXml = parse(time);

            // Attempt to parse the XML
            JAXBContext jc = JAXBContext.newInstance(TimeModel.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            return (TimeModel) unmarshaller.unmarshal(new StringReader(timeXml));

        } catch (Exception e) {
            throw new TimeException("Failed parsing time description: " + time + "\n" + timeXml, e);
        }
    }


    public static void main(String... args) throws TimeException, JAXBException {
        TimeParser parser = TimeParser.get();

        System.out.println(parser.parseModel("Mid-July - end October 2014.").toXml());
        System.out.println(parser.parseModel("9. - 15. january 2014").toXml());

    }

}
