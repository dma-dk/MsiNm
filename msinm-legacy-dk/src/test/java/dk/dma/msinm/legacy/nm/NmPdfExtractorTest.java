package dk.dma.msinm.legacy.nm;

import dk.dma.msinm.model.Message;
import dk.dma.msinm.model.MessageType;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Check parsing NtM PDF's
 */
public class NmPdfExtractorTest {

    @Test
    public void testNmPdfExtractor() throws URISyntaxException {

        File pdf = Paths.get(getClass().getResource("/2014 EfS 21.pdf").toURI()).toFile();
        assertNotNull(pdf);

        List<Message> notices = new ArrayList<>();
        try {
            NmPdfExtractor extractor = new NmPdfExtractor(pdf);
            extractor.extractNotices(notices);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(38, notices.size());

        Message msg = notices.get(0);
        assertEquals(new Integer(548), msg.getSeriesIdentifier().getNumber());
        assertEquals(MessageType.TEMPORARY_NOTICE, msg.getSeriesIdentifier().getType());
        assertEquals("104", msg.getCharts().get(0).getChartNumber());
        assertEquals(1, msg.getLocations().size());
        assertEquals(1, msg.getLocations().get(0).getPoints().size());
        assertNotNull(msg.getDesc("da"));
        assertTrue(msg.getDesc("da").getDescription().startsWith("I anførte tidsrum udføres geotekniske undersøgelser"));
        assertEquals("Geotekniske undersøgelser", msg.getDesc("da").getTitle());
        assertTrue(msg.getDesc("da").getTime().startsWith("Ultimo maj - primo juni 2014"));
        assertNotNull(msg.getArea());
        assertNotNull(msg.getArea().getParent());
        assertNotNull(msg.getArea().getParent().getParent());
        assertEquals("Kriegers Flak", msg.getArea().getDesc("da").getName());
        assertEquals("Danmark", msg.getArea().getParent().getParent().getDesc("da").getName());
        assertEquals("Østersøen", msg.getArea().getParent().getDesc("da").getName());

        assertNotNull(msg.getDesc("en"));
        assertEquals("Geotechnical surveys", msg.getDesc("en").getTitle());

    }
}
