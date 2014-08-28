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

import dk.dma.msinm.common.util.TextUtils;
import dk.dma.msinm.model.*;
import dk.dma.msinm.service.AreaService;
import dk.dma.msinm.service.ChartService;
import dk.dma.msinm.service.MessageService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 */
@Stateless
public class LegacyNmImportService {

    @Inject
    Logger log;

    @Inject
    MessageService messageService;

    @Inject
    AreaService areaService;

    @Inject
    ChartService chartService;

    /**
     * Imports the NtM template and returns the message if it was imported
     * @param template the NtM template
     * @param weekStartDate the start date of the week
     * @param txt a log of the import
     * @return the message actually imported
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Message importMessage(Message template, Date weekStartDate, StringBuilder txt) {
        // Check if the message already exists
        Message message = messageService.findBySeriesIdentifier(
                template.getSeriesIdentifier().getMainType(),
                template.getSeriesIdentifier().getNumber(),
                template.getSeriesIdentifier().getYear(),
                template.getSeriesIdentifier().getAuthority()
        );
        if (message != null) {
            log.warn("Message " + template.getSeriesIdentifier() + " already exists. Skipping");
            txt.append("Skipping existing NtM: " + template.getSeriesIdentifier() + "\n");
            return null;
        }

        // Fill out missing fields
        template.setValidFrom(weekStartDate);
        template.setPriority(Priority.NONE);
        template.setStatus(Status.PUBLISHED);

        // Some NM's do not have descriptions. Use the (html'ified) title instead
        template.getDescs().forEach(desc -> desc.setDescription(StringUtils.defaultIfBlank(desc.getDescription(), TextUtils.txt2html(desc.getTitle()))));

        try {
            // Make sure all charts are saved
            List<Chart> charts = findOrCreateCharts(template.getCharts());
            template.setCharts(charts);

            // Make sure the area, and parent areas, exists
            Area area = findOrCreateArea(template.getArea());
            template.setArea(area);

            // Save the message
            message = messageService.saveMessage(template);
            txt.append("Saved NtM: " + template.getSeriesIdentifier() + "\n");
        } catch (Exception e) {
            txt.append("Error saving NtM: " + template.getSeriesIdentifier() + "\n");
            log.error("Failed saving message " + message, e);
        }
        return message;
    }

    /**
     * Ensures that the template area and it's parents exists
     * @param templateArea the template area
     * @return the area
     */
    private Area findOrCreateArea(Area templateArea) {
        Area parent = null;
        if (templateArea.getParent() != null) {
            parent = findOrCreateArea(templateArea.getParent());
        }
        Integer parentId = (parent == null) ? null : parent.getId();

        Area area = areaService.findByName(templateArea.getDesc("da").getName(), "da", parentId);
        if (area == null) {
            area = areaService.createArea(templateArea, parentId);
        }
        return area;
    }

    /**
     * Ensures that the template charts area all saved
     * @param templateCharts the template charts
     * @return the actual charts
     */
    private List<Chart> findOrCreateCharts(List<Chart> templateCharts) {
        List<Chart> result = new ArrayList<>();
        templateCharts.forEach(templateChart -> {
            Chart chart = chartService.findByChartNumber(templateChart.getChartNumber());
            if (chart == null) {
                chart = chartService.createChart(templateChart);
            }
            result.add(chart);
        });
        return result;
    }

}

