package uk.ukho.msinm.legacy;

import dk.dma.msinm.common.MsiNmApp;
import dk.dma.msinm.model.*;
import dk.dma.msinm.service.AreaService;
import dk.dma.msinm.service.CategoryService;
import dk.dma.msinm.service.ChartService;
import dk.dma.msinm.service.MessageService;
import dk.dma.msinm.vo.MessageVo;
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
public class WeeklyNmImportService {

    @Inject
    Logger log;

    @Inject
    MsiNmApp app;

    @Inject
    MessageService messageService;

    @Inject
    AreaService areaService;

    @Inject
    CategoryService categoryService;

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
    public Message importMessage(MessageVo template, Date weekStartDate, StringBuilder txt) {
        // Check if the message already exists
        Message existingMessage = messageService.findBySeriesIdentifier(
                template.getSeriesIdentifier().getMainType(),
                template.getSeriesIdentifier().getNumber(),
                template.getSeriesIdentifier().getYear(),
                template.getSeriesIdentifier().getAuthority()
        );
        if (existingMessage != null) {
            log.warn("Message " + template.getSeriesIdentifier() + " already exists. Skipping");
            txt.append("Skipping existing NtM: " + template.getSeriesIdentifier() + "\n");
            return null;
        }

        // Fill out missing fields
        Message message = template.toEntity();
        message.setValidFrom(weekStartDate);
        message.setPriority(Priority.NONE);
        message.setStatus(Status.PUBLISHED);

        // Trim the NM titles, as the XSLT has left them with newlines, etc
        message.getDescs().stream()
                .filter(desc -> StringUtils.isNotBlank(desc.getTitle()))
                .forEach(desc -> desc.setTitle(desc.getTitle().replaceAll("\\s+", " ")));

        // Some NM's do not have descriptions. Use the title instead
        message.getDescs()
                .forEach(desc -> desc.setDescription(StringUtils.defaultIfBlank(desc.getDescription(), desc.getTitle())));

        // Update the location type, depending on the number of points
        // By default, they have been set to be polygons.
        message.getLocations().forEach(loc -> {
            if (loc.getPoints().size() == 1) {
                loc.setType(Location.LocationType.POINT);
            } else if (loc.getPoints().size() == 2) {
                loc.setType(Location.LocationType.POLYLINE);
            }
        });

        try {
            // Make sure all charts are saved
            List<Chart> charts = findOrCreateCharts(message.getCharts());
            message.setCharts(charts);

            // Make sure the area, and parent areas, exists
            Area area = findOrCreateArea(message.getArea());
            message.setArea(area);

            // Make sure the categories are saved. NB: UK only use one level of categories
            List<Category> categories = findOrCreateCategories(message.getCategories());
            message.setCategories(categories);

            // Save the message
            message = messageService.saveMessage(message);
            txt.append("Saved NtM: " + message.getSeriesIdentifier() + "\n");
        } catch (Exception e) {
            txt.append("Error saving NtM: " + message.getSeriesIdentifier() + "\n");
            log.error("Failed saving message " + message, e);
        }
        return message;
    }

    /**
     * Ensures that the template area and it's parents exists
     * @param templateArea the template area
     * @return the area
     */
    //@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    private Area findOrCreateArea(Area templateArea) {
        Area parent = null;
        if (templateArea.getParent() != null) {
            parent = findOrCreateArea(templateArea.getParent());
        }
        Integer parentId = (parent == null) ? null : parent.getId();

        Area area = areaService.findByName(templateArea.getDesc("en").getName(), "en", parentId);
        if (area == null) {
            area = new Area();
            area.copyDescsAndRemoveBlanks(templateArea.getDescs());
            area.setLocations(templateArea.getLocations());
            area = areaService.createArea(area, parentId);
        }
        return area;
    }

    /**
     * Ensures that the template charts area all saved
     * @param templateCharts the template charts
     * @return the actual charts
     */
    //@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
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

    /**
     * Ensures that the template categories are all saved
     * NB: We know that UK only use one level of categories!
     * @param templateCategories the template categories
     * @return the actual categories
     */
    //@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    private List<Category> findOrCreateCategories(List<Category> templateCategories) {
        List<Category> result = new ArrayList<>();
        templateCategories.forEach(templateCategory -> {
            Category category = categoryService.findByName(templateCategory.getDesc("en").getName(), "en", null);
            if (category == null) {
                category = new Category();
                category.copyDescsAndRemoveBlanks(templateCategory.getDescs());
                category = categoryService.createCategory(category, null);
            }
            result.add(category);
        });
        return result;
    }

}
