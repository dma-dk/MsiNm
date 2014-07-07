/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.msinm.legacy.nm;

import dk.dma.msinm.model.Message;
import dk.dma.msinm.model.Type;
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
        assertEquals(Type.TEMPORARY_NOTICE, msg.getType());
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
