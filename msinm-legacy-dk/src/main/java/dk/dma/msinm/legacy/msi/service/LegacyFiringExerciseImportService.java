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
package dk.dma.msinm.legacy.msi.service;

import dk.dma.msinm.common.MsiNmApp;
import dk.dma.msinm.common.db.Sql;
import dk.dma.msinm.common.model.DataFilter;
import dk.dma.msinm.common.sequence.DefaultSequence;
import dk.dma.msinm.common.sequence.Sequence;
import dk.dma.msinm.common.sequence.Sequences;
import dk.dma.msinm.common.service.BaseService;
import dk.dma.msinm.common.settings.DefaultSetting;
import dk.dma.msinm.common.settings.Setting;
import dk.dma.msinm.common.settings.Settings;
import dk.dma.msinm.common.util.TimeUtils;
import dk.dma.msinm.model.Area;
import dk.dma.msinm.model.Category;
import dk.dma.msinm.model.Chart;
import dk.dma.msinm.model.Location;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.model.Point;
import dk.dma.msinm.model.SeriesIdType;
import dk.dma.msinm.model.SeriesIdentifier;
import dk.dma.msinm.model.Status;
import dk.dma.msinm.model.Type;
import dk.dma.msinm.service.AreaService;
import dk.dma.msinm.service.CategoryService;
import dk.dma.msinm.service.ChartService;
import dk.dma.msinm.service.MessageService;
import dk.dma.msinm.vo.LocationVo;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Imports firing exercises from a local db dump of the Danish MSI database
 */
@Stateless
public class LegacyFiringExerciseImportService extends BaseService {

    /**
     * Defines whether the import is active or not.
     * By default, the import is not active
     */
    public static final Setting LEGACY_FIRING_EXERCISE_ACTIVE = new DefaultSetting("legacyFiringExerciseImportActive", "false");

    Pattern CHART_PATTERN_1 = Pattern.compile("(\\d+)");
    Pattern CHART_PATTERN_2 = Pattern.compile("(\\d+) \\(INT (\\d+)\\)");


    @Inject
    Logger log;

    @Inject
    Sequences sequences;

    @Inject
    LegacyDatabase legacyDatabase;

    @Inject
    AreaService areaService;

    @Inject
    ChartService chartService;

    @Inject
    CategoryService categoryService;

    @Inject
    MessageService messageService;

    @Inject
    MsiNmApp app;

    @Inject
    @Sql("/sql/firing_areas.sql")
    String firingAreasSql;

    @Inject
    @Sql("/sql/active_firing_exercises.sql")
    String activeFiringExercisesSql;

    @Inject
    Settings settings;

    /**
     * Import active firing exercises
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public List<Message> importFiringExercises(){

        // Check if the integration is active
        if (!settings.getBoolean(LEGACY_FIRING_EXERCISE_ACTIVE)) {
            return new ArrayList<>();
        }

        Connection conn = null;
        try {
            conn = legacyDatabase.getConnection();

            // Create all legacy firing areas
            log.debug("Start importing at firing areas from local DB");
            Map<String, Area> areaLookup = createFiringAreas(conn);

            // Create the "Firing Exercises" category
            log.debug("Creating 'Firing Exercises' category");
            Category category = categoryService.findOrCreateFiringExercisesCategory();
            // Ensure that it is translated to Danish
            if (category.getDesc("da") == null) {
                category.checkCreateDesc("da").setName("Skydeøvelser");
                category = saveEntity(category);
            }

            // Create or update active firing exercises
            log.debug("Creating firing exercises");
            return checkCreateFiringExercises(conn, areaLookup, category);

        } catch(Exception ex) {
            log.error("Failed importing legacy firing exercises ", ex);
        } finally {
            try { conn.close(); } catch (Exception ex) { }
        }

        return new ArrayList<>();
    }

    /**
     * Creates or updates MSI for active firing exercises
     * @param conn the DB connection
     * @param areaLookup the firing areas
     * @param category the firing exercises category
     * @return created or updated messages
     */
    private List<Message> checkCreateFiringExercises(Connection conn, Map<String, Area> areaLookup, Category category) {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(activeFiringExercisesSql);

            // First create all firing areas
            ResultSet rs = stmt.executeQuery();
            List<Message> messages = new ArrayList<>();
            Message msg = null;
            Integer lastId = null;
            while (rs.next()) {
                Integer id              = LegacyDatabase.getInt(rs,    "id");
                String  areaDa          = LegacyDatabase.getString(rs, "area_da");
                String  descriptionEn   = LegacyDatabase.getString(rs, "description_en");
                String  descriptionDa   = LegacyDatabase.getString(rs, "description_da");
                Date    validFrom       = LegacyDatabase.getDate(rs,   "valid_from");
                Date    validTo         = LegacyDatabase.getDate(rs,   "valid_to");
                Integer infoType        = LegacyDatabase.getInt(rs,    "info_type");

                if (!id.equals(lastId)) {
                    // Add the previous message
                    if (msg != null) {
                        messages.add(msg);
                    }

                    lastId = id;
                    msg = new Message();
                    SeriesIdentifier serId = new SeriesIdentifier();
                    msg.setSeriesIdentifier(serId);
                    serId.setAuthority(app.getOrganization());
                    serId.setYear(Calendar.getInstance().get(Calendar.YEAR));
                    serId.setMainType(SeriesIdType.MSI);
                    msg.setType(Type.SUBAREA_WARNING);
                    msg.setStatus(Status.DRAFT);
                    msg.createDesc("da").setTitle("Skydeøvelser. Advarsel");
                    msg.createDesc("en").setTitle("Firing Exercises. Warning");
                    msg.setValidFrom(TimeUtils.resetSeconds(validFrom));
                    msg.setValidTo(TimeUtils.resetSeconds(validTo));
                    formatTime(msg, "da");
                    formatTime(msg, "en");
                    msg.getCategories().add(category);
                    Area area = areaLookup.get(areaDa);
                    msg.setArea(area);
                    msg.getLocations().addAll(copyLocations(area.getLocations()));
                }

                // Copy various info types
                if (infoType == 1) {
                    // Details
                    msg.getDesc("da").setDescription(descriptionDa);
                    msg.getDesc("en").setDescription(descriptionEn);

                } else if (infoType == 3) {
                    // Charts
                    String charts = descriptionDa.replaceAll("\\.", "");
                    for (String chartStr : charts.split(",")) {
                        Matcher m1 = CHART_PATTERN_1.matcher(chartStr.trim());
                        Matcher m2 = CHART_PATTERN_2.matcher(chartStr.trim());
                        String chartNumber = (m1.matches()) ? m1.group(1) : ((m2.matches()) ? m2.group(1) : null);
                        Chart chart = chartService.findByChartNumber(chartNumber);
                        if (chart != null) {
                            msg.getCharts().add(chart);
                        }
                    }
                }
            }
            rs.close();

            // Add the last message
            if (msg != null) {
                messages.add(msg);
            }

            // Persist the messages
            return checkCreateMessages(messages, category);

        } catch(Exception ex) {
            log.error("Failed fetching firing exercises ", ex);
        } finally {
            try { stmt.close(); } catch (Exception ex) { }
        }
        return new ArrayList<>();
    }


    /**
     * Checks if the given message needs to be persisted
     * @param messages the list of messages to persist
     * @return the persisted messages
     */
    private List<Message> checkCreateMessages(List<Message> messages, Category category) {

        List<Message> result = new ArrayList<>();
        List<Message> current = new ArrayList<>(messageService.findPublishedMessagesByCategory(category));
        Date tomorrow = TimeUtils.resetTime(new Date(System.currentTimeMillis() + 1 * 24 * 60 * 60 * 1000L));

        // Match up the message with existing firing exercises and remove matches. After the loop, we have:
        // The messages left in the "messages" list needs to be persisted
        // The messages left in the "current" list needs to be cancelled
        for (ListIterator<Message> msgIt = messages.listIterator(); msgIt.hasNext(); ) {
            Message msg = msgIt.next();
            for (ListIterator<Message> curIt = current.listIterator(); curIt.hasNext(); ) {
                Message curMsg = curIt.next();
                if (msg.getArea().getId().equals(curMsg.getArea().getId()) &&
                        TimeUtils.sameDateHourMinute(msg.getValidFrom(), curMsg.getValidFrom()) &&
                        TimeUtils.sameDateHourMinute(msg.getValidTo(), curMsg.getValidTo())) {
                    // This is a matching MSI - remove from the lists
                    msgIt.remove();
                    curIt.remove();

                } else if (curMsg.getSeriesIdentifier().getMainType() == SeriesIdType.NM ||
                        TimeUtils.resetTime(curMsg.getValidFrom()).after(tomorrow)) {
                    // Also remove NM's and future (after tomorrow) MSI's from the list
                    curIt.remove();
                }
            }
        }

        // Cancel remaining messages left in the "current" list

        current.forEach(msg -> result.add(messageService.setStatus(msg.getId(), Status.CANCELLED)));

        // Create the remaining messages left in the "messages" list
        messages.forEach(msg -> result.add(createMessage(msg)));

        return result;
    }

    /**
     * Formats the time interval
     * @param msg the message
     * @param lang the language
     */
    private void formatTime(Message msg, String lang) {
        try {
            if (TimeUtils.sameDate(msg.getValidFrom(), msg.getValidTo())) {
                SimpleDateFormat sdf1 = new SimpleDateFormat("da".equals(lang) ? "d MMMM yyyy, 'kl.' HH:mm" : "d MMMM yyyy, 'hours' HH:mm", new Locale(lang));
                SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
                msg.getDesc(lang).setTime(String.format("%s - %s", sdf1.format(msg.getValidFrom()), sdf2.format(msg.getValidTo())));
            }
        } catch (Exception e) {
            log.warn("Failed formatting time for message " + msg + ": " + e);
        }
    }

    /**
     * Persists the message
     * @param message the message
     * @return the persisted message
     */
    private Message createMessage(Message message) {
        // Save the message draft
        message = saveEntity(message);

        // Saving the draft went fine. Assign a series identifier.
        Sequence sequence = new DefaultSequence(
                "LEGACY_MESSAGE_SERIES_ID_MSI_"
                        + message.getSeriesIdentifier().getAuthority() + "_"
                        + message.getSeriesIdentifier().getYear(), 1000);

        message.getSeriesIdentifier().setNumber((int) sequences.getNextValue(sequence));
        message.setStatus(Status.PUBLISHED);

        message = messageService.saveMessage(message);
        log.info("Persisted new firing exercise message " + message);
        return message;
    }

    /**
     * Copies the list of locations
     * @param locations the locations to copy
     * @return the copied locations
     */
    private List<Location> copyLocations(List<Location> locations) {
        final List<Location> result = new ArrayList<>();
        locations.forEach(loc -> {
            // Trick to copy the location by converting to and fro the VO
            LocationVo locVo = new LocationVo(loc, DataFilter.get(DataFilter.ALL));
            result.add(locVo.toEntity());
        });
        return result;
    }

    /**
     * Creates all legacy firing areas
     * @param conn the DB connection
     * @return a lookup map for areas
     */
    private Map<String, Area> createFiringAreas(Connection conn) {
        final Map<String, Area> areaLookup = new HashMap<>();

        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(firingAreasSql);

            // First create all firing areas
            ResultSet rs = stmt.executeQuery();
            List<Area> firingAreas = new ArrayList<>();
            Area area = null;
            Integer lastId = null;
            while (rs.next()) {
                Integer id          = LegacyDatabase.getInt(rs,     "id");
                String  area1En     = LegacyDatabase.getString(rs,  "area1_en");
                String  area1Da     = LegacyDatabase.getString(rs,  "area1_da");
                String  area2En     = LegacyDatabase.getString(rs,  "area2_en");
                String  area2Da     = LegacyDatabase.getString(rs,  "area2_da");
                Integer latDeg      = LegacyDatabase.getInt(rs,     "lat_deg");
                Double  latMin      = LegacyDatabase.getDouble(rs,  "lat_min");
                Integer lonDeg      = LegacyDatabase.getInt(rs,     "lon_deg");
                Double  lonMin      = LegacyDatabase.getDouble(rs,  "lon_min");

                if (!id.equals(lastId)) {
                    lastId = id;
                    Area parent = LegacyMsiImportService.createAreaTemplate("Denmark", "Danmark", null);
                    parent = LegacyMsiImportService.createAreaTemplate(area1En, area1Da, parent);
                    area = LegacyMsiImportService.createAreaTemplate(area2En, area2Da, parent);
                    area.getLocations().add(new Location());
                    area.getLocations().get(0).setType(Location.LocationType.POLYGON);
                    firingAreas.add(area);
                }

                double lat = latDeg.doubleValue() + latMin / 60.0;
                double lon = lonDeg.doubleValue() + lonMin / 60.0;
                Location loc = area.getLocations().get(0);
                loc.getPoints().add(new Point(loc, lat, lon, loc.getPoints().size() + 1));

            }
            rs.close();

            // Persist the areas
            firingAreas.forEach(a -> areaLookup.put(a.getDesc("da").getName(), areaService.findOrCreateArea(a)));

        } catch(Exception ex) {
            log.error("Failed fetching firing areas ", ex);
        } finally {
            try { stmt.close(); } catch (Exception ex) { }
        }

        return areaLookup;
    }

}
