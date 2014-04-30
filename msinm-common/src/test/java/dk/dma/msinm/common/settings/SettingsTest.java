package dk.dma.msinm.common.settings;

import org.junit.Test;

import java.util.regex.Matcher;

import static org.junit.Assert.assertEquals;

/**
 * Test settings
 */
public class SettingsTest {


    @Test
    public void test() {

        String result = "${user.home}/.epd-ship";
        for (Object key : System.getProperties().keySet()) {
            result = result.replaceAll("\\$\\{" + key + "\\}", Matcher.quoteReplacement(System.getProperty("" + key)));
        }

        assertEquals(System.getProperty("user.home") + "/.epd-ship", result);
    }

}
