package dk.dma.msinm.common;

import dk.dma.msinm.common.time.TimeException;
import dk.dma.msinm.common.time.TimeModel;
import dk.dma.msinm.common.time.TimeParser;
import dk.dma.msinm.common.time.TimeTranslator;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test the TimeParser and TimeTranslator
 */
public class TimeTest {

    @Test
    public void timeParserTest() throws IOException, TimeException, JAXBException {
        List<String> times = readTestFile("/timeParserInput.txt");
        List<String> correctResults = readTestFile("/timeParserOutput.txt");

        assertEquals(times.size(), correctResults.size());

        TimeParser parser = TimeParser.get();

        for (int x = 0; x < times.size(); x++) {
            String time = times.get(x);
            String correctResult = correctResults.get(x);

            TimeModel result = parser.parseModel(time);
            //System.out.printf("%s%n", result.toXml());
            System.out.printf("==Input==%n%s%n==Output==%n%s%n", time, result.toXml());

            assertEquals(correctResult.trim(), result.toXml().trim());
        }
    }


    @Test
    public void timeTranslatorTest() throws TimeException, IOException {
        List<String> times = readTestFile("/timeParserInput.txt");

        TimeTranslator translator = TimeTranslator.get("da");

        for (int x = 0; x < times.size(); x++) {
            String time = times.get(x);

            String translated = translator.translateFromEnglish(time);
            String backToEnglish = translator.translateToEnglish(translated);

            System.out.printf("==Input==%n%s%n==Translated DK==%n%s%n==Translated EN==%n%s%n%n", time, translated, backToEnglish);

            // Assert that translating EN -> DA -> EN will be (almost) identical
            assertEquals(time.toLowerCase().trim(), backToEnglish.toLowerCase().trim());
        }
    }

    @Test
    public void timeParserDaTest() throws TimeException, IOException, JAXBException {
        List<String> times = readTestFile("/timeParserInput_da.txt");

        TimeParser parser = TimeParser.get();

        for (int x = 0; x < times.size(); x++) {
            String time = times.get(x);

            TimeModel result = parser.parseModel(time, "da");
            //System.out.printf("%s%n", result.toXml());
            System.out.printf("==Input==%n%s%n==Output==%n%s%n", time, result.toXml());
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
