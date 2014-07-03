package dk.dma.msinm.common;

import dk.dma.msinm.common.time.TimeException;
import dk.dma.msinm.common.time.TimeParser;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test the TimeParser
 */
public class TimeParserTest {

    @Test
    public void timeParserTest() throws IOException, TimeException {
        List<String> times = readTestFile("/timeParserInput.txt");
        List<String> correctResults = readTestFile("/timeParserOutput.txt");

        assertEquals(times.size(), correctResults.size());

        TimeParser parser = TimeParser.get("en");
        Date now = new Date();

        for (int x = 0; x < times.size(); x++) {
            String time = times.get(x);
            String correctResult = correctResults.get(x);

            String result = parser.parse(time, now);
            //System.out.printf("%s%n", result);
            System.out.printf("==Input==%n%s%n==Output==%n%s%n", time, result);

            assertEquals(correctResult.trim(), result.trim());
        }
    }


    List<String> readTestFile(String file) throws IOException {
        List<String> result = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(file)))) {

            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                if (StringUtils.isBlank(line)) {
                    if (sb.length() > 0) {
                        result.add(sb.toString().trim());
                        sb = new StringBuilder();
                    }
                    continue;
                }
                sb.append(line + "\n");
            }
            if (sb.length() > 0) {
                result.add(sb.toString().trim());
            }
        }
        return result;
    }

}
