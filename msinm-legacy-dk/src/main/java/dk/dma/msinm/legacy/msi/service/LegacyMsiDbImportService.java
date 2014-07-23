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
import dk.dma.msinm.common.sequence.DefaultSequence;
import dk.dma.msinm.common.sequence.Sequence;
import dk.dma.msinm.common.sequence.Sequences;
import dk.dma.msinm.common.service.BaseService;
import dk.dma.msinm.common.settings.DefaultSetting;
import dk.dma.msinm.common.settings.Setting;
import dk.dma.msinm.common.settings.Settings;
import dk.dma.msinm.common.settings.SettingsEntity;
import dk.dma.msinm.common.util.TextUtils;
import dk.dma.msinm.legacy.msi.model.LegacyMessage;
import dk.dma.msinm.model.*;
import dk.dma.msinm.service.MessageService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Imports data from a local db dump of the Danish MSI database
 */
@Stateless
public class LegacyMsiDbImportService extends BaseService {

    static final int LIMIT = 100; // Import at most 1000 MSI at a time

    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final Setting DB_URL     = new DefaultSetting("legacyMsiDbUrl", "jdbc:mysql://localhost:3306/oldmsi");
    static final Setting DB_USER    = new DefaultSetting("legacyMsiDbUser", "oldmsi");
    static final Setting DB_PWD     = new DefaultSetting("legacyMsiDbPassword", "oldmsi");

    static final Setting OLD_MSG_FIRST_DATE   = new DefaultSetting("legacyMsiFirstChangeDate"); // Defaults to today
    static final Setting ACTIVE_MSG_LAST_DATE = new DefaultSetting("legacyMsiLastChangeDate", "0"); // init with 1970

    @Inject
    Logger log;

    @Inject
    MsiNmApp app;

    @Inject
    MessageService messageService;

    @Inject
    LegacyMessageService legacyMessageService;

    @Inject
    Sequences sequences;

    @Inject
    @Sql("/sql/legacy_msi_data.sql")
    String legacyMsiDataSql;

    @Inject
    @Sql("/sql/active_legacy_msi.sql")
    String activeLegacyMsiSql;

    @Inject
    @Sql("/sql/old_legacy_msi.sql")
    String oldLegacyMsiSql;

    @Inject
    Settings settings;

    /**
     * Ensure that the DB driver is loaded
     */
    @PostConstruct
    public void init() throws ClassNotFoundException {
        Class.forName(JDBC_DRIVER);
    }

    /**
     * Import active MSI warnings
     * @return the imported/updated MSI warnings
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public List<LegacyMessage> importActiveMsiWarnings() {
        String sql = activeLegacyMsiSql
                .replace(":limit", String.valueOf(LIMIT));

        Date validFrom = new Date();
        Date lastRegisteredUpdateDate = settings.getDate(ACTIVE_MSG_LAST_DATE);
        List<LegacyMessage> result = importMsi(sql, "active", validFrom, lastRegisteredUpdateDate);

        // Register the new last update time
        Date lastUpdateDate = result.stream()
                        .map(LegacyMessage::getUpdated)
                        .max(Date::compareTo)
                        .orElse(lastRegisteredUpdateDate);
        settings.updateSetting(new SettingsEntity(
                ACTIVE_MSG_LAST_DATE.getSettingName(),
                String.valueOf(lastUpdateDate.getTime())
            ));

        return result;
    }

    /**
     * Import active MSI warnings
     * @param sql the sql for fetching IDS
     * @return the imported/updated MSI warnings
     */
    public List<LegacyMessage> importMsi(String sql, String type, Date... dataParams) {
        log.info("Start importing at most " + LIMIT + " " + type + " legacy MSI warnings from local DB");
        List<LegacyMessage> result = new ArrayList<>();

        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DriverManager.getConnection(
                    settings.get(DB_URL),
                    settings.get(DB_USER),
                    settings.get(DB_PWD));

            stmt = conn.prepareStatement(sql);

            // Set the parameters, which must consist of a set of data parameters,
            // and lastly a limit parameter
            for (int x = 0; x < dataParams.length; x++) {
                stmt.setTimestamp(x + 1, new Timestamp(dataParams[x].getTime()));
            }
            stmt.setInt(dataParams.length + 1, LIMIT);

            log.info("Executing SQL\n" + sql);
            long t0 = System.currentTimeMillis();

            // Fetch ID's of active MSI
            List<Integer> ids = new ArrayList<>();
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
            rs.close();
            log.info(String.format("Fetched %d ID's for %s legacy MSI in %d ms", ids.size(), type, System.currentTimeMillis() - t0));

            // Import the MSI's
            if (ids.size() > 0) {
                importMsi(ids, conn, result);
            }

        } catch(Exception ex) {
            log.error("Failed fetching active legacy MSI messages from database ", ex);
        } finally {
            try { stmt.close(); } catch (Exception ex) { }
            try { conn.close(); } catch (Exception ex) { }
        }

        return result;
    }

    /**
     * Import the legacy MSI with the given ID's
     * @param ids the ID's of the MSI to import
     * @param conn the DB connection
     * @return the result
     */
    private List<LegacyMessage> importMsi(List<Integer> ids, Connection conn, List<LegacyMessage> result) {
        log.info("Start importing at most " + LIMIT + " legacy MSI warnings from local DB");
        long t0 = System.currentTimeMillis();
        int count = 0;

        Statement stmt = null;
        try {
            stmt = conn.createStatement();

            String sql = legacyMsiDataSql
                    .replace(":ids", StringUtils.join(ids, ","));

            log.info("Executing SQL\n" + sql);
            ResultSet rs = stmt.executeQuery(sql);

            Integer skipId = null;
            LegacyMessage legacyMessage = null;
            while (rs.next()) {
                Integer id                  = getInt(rs,     "id");
                Integer messageId           = getInt(rs,     "messageId");
                Boolean statusDraft         = getBoolean(rs, "statusDraft");
                String  navtexNo            = getString(rs,  "navtexNo");
                String  descriptionEn       = getString(rs,  "description_en");
                String  descriptionDa       = getString(rs,  "description_da");
                String  title               = getString(rs,  "title");
                Date    validFrom           = getDate(rs,    "validFrom");
                Date    validTo             = getDate(rs,    "validTo");
                Date    created             = getDate(rs,    "created");
                Date    updated             = getDate(rs,    "updated");
                Date    deleted             = getDate(rs,    "deleted");
                Integer version             = getInt(rs,     "version");
                String  priority            = getString(rs,  "priority");
                String  messageType         = getString(rs,  "messageType");
                String  category1En         = getString(rs,  "category1_en");
                String  category1Da         = getString(rs,  "category1_da");
                String  category2En         = getString(rs,  "category2_en");
                String  category2Da         = getString(rs,  "category2_da");
                String  area1En             = getString(rs,  "area1_en");
                String  area1Da             = getString(rs,  "area1_da");
                String  area2En             = getString(rs,  "area2_en");
                String  area2Da             = getString(rs,  "area2_da");
                String  area3En             = getString(rs,  "area3_en");
                String  area3Da             = getString(rs,  "area3_da");
                String  locationType        = getString(rs,  "locationType");
                Integer pointIndex          = getInt(rs,     "pointIndex");
                Double  pointLatitude       = getDouble(rs,  "pointLatitude");
                Double  pointLongitude      = getDouble(rs,  "pointLongitude");
                Integer pointRadius         = getInt(rs,     "pointRadius");

                if (skipId != null && skipId.intValue() == id.intValue()) {
                    continue;
                }

                if (legacyMessage != null && !legacyMessage.getLegacyId().equals(id)) {
                    saveMessage(legacyMessage, count++);
                    legacyMessage = null;
                }

                // Handle first record of a new message
                if (legacyMessage == null) {

                    // First check if a legacy message with the given id already exists
                    legacyMessage = legacyMessageService.findByLegacyId(id);
                    if (legacyMessage != null && legacyMessage.getVersion() >= version) {
                        // Skip the import
                        legacyMessage = null;
                        skipId = id;
                        continue;

                    } else if (legacyMessage == null) {
                        // Create a new legacy message
                        legacyMessage = new LegacyMessage();
                        legacyMessage.setMessage(new Message());
                    } else if (legacyMessage != null) {
                        legacyMessage.getMessage().preload();
                        em.detach(legacyMessage);
                    }

                    result.add(legacyMessage);
                    Message message = legacyMessage.getMessage();

                    // Update legacy message
                    legacyMessage.setLegacyId(id);
                    legacyMessage.setNavtexNo(navtexNo);
                    legacyMessage.setVersion(version);
                    legacyMessage.setUpdated(updated);

                    // Create the message series identifier
                    SeriesIdentifier identifier = new SeriesIdentifier();
                    identifier.setMainType(SeriesIdType.MSI);
                    message.setSeriesIdentifier(identifier);
                    if (StringUtils.isNotBlank(navtexNo) && navtexNo.split("-").length == 3) {
                        // Extract the series identifier from the navtext number
                        String[] parts = navtexNo.split("-");
                        identifier.setAuthority(parts[0]);
                        identifier.setNumber(Integer.valueOf(parts[1]));
                        identifier.setYear(2000 + Integer.valueOf(parts[2]));

                    } else {
                        // Some legacy MSI do not have a navtex number.
                        // Give them a number > 1000, since these are unused
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(validFrom);
                        int year = cal.get(Calendar.YEAR);

                        Sequence sequence = new DefaultSequence("LEGACY_MESSAGE_SERIES_ID_MSI_" + app.getOrganization() + "_" + year, 1000);

                        identifier.setNumber((int) sequences.getNextValue(sequence));
                        identifier.setAuthority(app.getOrganization());
                        identifier.setYear(year);
                    }

                    // Message data
                    message.setCreated(created);
                    message.setUpdated(updated);
                    if ("Navtex".equals(messageType) || "Navwarning".equals(messageType)) {
                        message.setType(Type.SUBAREA_WARNING);
                    } else {
                        message.setType(Type.COSTAL_WARNING);
                    }
                    message.setStatus(deleted != null ? Status.DELETED : (statusDraft ? Status.DRAFT : Status.PUBLISHED));
                    message.setValidFrom(validFrom);
                    message.setValidTo((validTo != null) ? validTo : deleted);
                    try {
                        message.setPriority(Priority.valueOf(priority));
                    } catch (Exception ex) {
                        message.setPriority(Priority.NONE);
                    }

                    // Message Desc
                    if (StringUtils.isNotBlank(title) || StringUtils.isNotBlank(descriptionEn) || StringUtils.isNotBlank(area3En)) {
                        MessageDesc descEn = message.checkCreateDesc("en");
                        descEn.setTitle(StringUtils.defaultString(title, descriptionEn));
                        descEn.setDescription(TextUtils.txt2html(descriptionEn));
                        descEn.setVicinity(area3En);
                    }
                    if (StringUtils.isNotBlank(title) || StringUtils.isNotBlank(descriptionDa) || StringUtils.isNotBlank(area3Da)) {
                        MessageDesc descDa = message.checkCreateDesc("da");
                        descDa.setTitle(StringUtils.defaultString(title, descriptionDa));
                        descDa.setDescription(TextUtils.txt2html(descriptionDa));
                        descDa.setVicinity(area3Da);
                    }

                    // Areas
                    int msgVersion = message.getVersion();
                    Area area = legacyMessageService.findOrCreateArea(area1En, area1Da, null);
                    area = legacyMessageService.findOrCreateArea(area2En, area2Da, area);
                    message.setArea(area);

                    // Categories
                    Category category = legacyMessageService.findOrCreateCategory(category1En, category1Da, null);
                    category = legacyMessageService.findOrCreateCategory(category2En, category2Da, category);
                    if (category != null) {
                        message.getCategories().clear();
                        message.getCategories().add(category);
                    }

                    message.setVersion(msgVersion);

                    // Locations
                    message.getLocations().clear();
                    if (pointLatitude != null) {
                        Location.LocationType type;
                        switch (locationType) {
                            case "Point":       type = Location.LocationType.POINT; break;
                            case "Polygon":     type = Location.LocationType.POLYGON; break;
                            case "Points":      type = Location.LocationType.POLYLINE; break;
                            case "Polyline":    type = Location.LocationType.POLYLINE; break;
                            default:            type = Location.LocationType.POLYLINE;
                        }
                        Location loc1 = new Location(type);
                        if (pointRadius != null) {
                            loc1.setRadius(pointRadius);
                        }
                        message.getLocations().add(loc1);
                    }
                }

                if (pointLatitude != null) {
                    Location loc1 = legacyMessage.getMessage().getLocations().get(0);
                    // If the type of the location is POINT, there must only be one point per location
                    if (loc1.getType() == Location.LocationType.POINT && loc1.getPoints().size() > 0) {
                        loc1 = new Location(Location.LocationType.POINT);
                        legacyMessage.getMessage().getLocations().add(loc1);
                    }
                    loc1.addPoint(new Point(loc1, pointLatitude, pointLongitude, pointIndex));
                }
            }

            if (legacyMessage != null) {
                saveMessage(legacyMessage, count++);
            }

            log.info(String.format("Import completed in %d ms", System.currentTimeMillis() - t0));

            rs.close();
            stmt.close();
            conn.close();
        } catch(Exception ex) {
            log.error("Failed fetching legacy MSI messages from database ", ex);
        } finally {
            try { stmt.close(); } catch (Exception ex) { }
            try { conn.close(); } catch (Exception ex) { }
        }

        return result;
    }


    /**
     * Persists the legacy message
     * @param legacyMsg the legacy message to persist
     * @param count the message index
     */
    private void saveMessage(LegacyMessage legacyMsg, int count) {
        try {
            // Check the location to make it valid
            Location loc = legacyMsg.getMessage().getLocations().get(0);
            if (loc != null && loc.getType() == Location.LocationType.POLYGON && loc.getPoints().size() < 3) {
                loc.setType(Location.LocationType.POLYLINE);
            }

            legacyMessageService.saveLegacyMessage(legacyMsg);
            log.info("Saved message " + count);
        } catch (Exception ex) {
            log.error("Failed persisting message " + legacyMsg, ex);
        }
    }

    private String getString(ResultSet rs, String key) throws SQLException {
        String val = rs.getString(key);
        return rs.wasNull() ? null : val;
    }

    private Integer getInt(ResultSet rs, String key) throws SQLException {
        Integer val = rs.getInt(key);
        return rs.wasNull() ? null : val;
    }

    private Double getDouble(ResultSet rs, String key) throws SQLException {
        Double val = rs.getDouble(key);
        return rs.wasNull() ? null : val;
    }

    private Date getDate(ResultSet rs, String key) throws SQLException {
        Timestamp val = rs.getTimestamp(key);
        return rs.wasNull() ? null : val;
    }


    private Boolean getBoolean(ResultSet rs, String key) throws SQLException {
        boolean val = rs.getBoolean(key);
        return rs.wasNull() ? null : val;
    }
}
