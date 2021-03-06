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

import dk.dma.msinm.model.SeriesIdentifier;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Check parsing active list of P&T NtM PDF's
 */
@Ignore // Certain JDK versions err with "Input length must be multiple of 16 when decrypting with padded cipher"
public class ActiveTempPrelimNmPdfExtractorTest {

    @Test
    public void testActiveTempPrelimNmPdfExtractor() throws URISyntaxException {

        File pdf = Paths.get(getClass().getResource("/2014 PogT 21.pdf").toURI()).toFile();
        assertNotNull(pdf);

        List<SeriesIdentifier> noticeIds = new ArrayList<>();
        try {
            ActiveTempPrelimNmPdfExtractor extractor = new ActiveTempPrelimNmPdfExtractor(pdf, "DMA");
            extractor.extractActiveNoticeIds(noticeIds);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(132, noticeIds.size());

        // Match "18/382 (T) Kroghage Dyb..."
        assertEquals(new Integer(382), noticeIds.get(0).getNumber());

        // Match "19/487 (P) Hjelm Bugt..."
        assertEquals(new Integer(487), noticeIds.get(2).getNumber());

        // Match "15-16/383 (T) Møn SE. Hjelm Bugt...."
        assertEquals(new Integer(383), noticeIds.get(3).getNumber());
    }
}
