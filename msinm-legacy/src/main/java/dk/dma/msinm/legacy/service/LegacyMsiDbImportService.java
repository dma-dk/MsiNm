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
package dk.dma.msinm.legacy.service;

import dk.dma.msinm.common.db.Sql;
import dk.dma.msinm.common.sequence.Sequences;
import dk.dma.msinm.common.settings.DefaultSetting;
import dk.dma.msinm.common.settings.Setting;
import dk.dma.msinm.common.settings.Settings;
import dk.dma.msinm.legacy.model.LegacyMessage;
import dk.dma.msinm.model.GeneralCategory;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.model.MessageCategory;
import dk.dma.msinm.model.MessageItem;
import dk.dma.msinm.model.MessageLocation;
import dk.dma.msinm.model.MessageSeriesIdentifier;
import dk.dma.msinm.model.MessageStatus;
import dk.dma.msinm.model.MessageType;
import dk.dma.msinm.model.NavwarnMessage;
import dk.dma.msinm.model.Point;
import dk.dma.msinm.model.Priority;
import dk.dma.msinm.model.SpecificCategory;
import dk.dma.msinm.service.MessageService;
import org.apache.commons.lang.StringUtils;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.slf4j.Logger;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

/**
 * Imports data from a local db dump
 */
@Path("/import/legacy_msi")
@Stateless
@SecurityDomain("msinm-policy")
@RolesAllowed({ "admin" })
public class LegacyMsiDbImportService {

    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final Setting DB_URL     = new DefaultSetting("legacyMsiDbUrl", "jdbc:mysql://localhost:3306/oldmsi");
    static final Setting DB_USER    = new DefaultSetting("legacyMsiDbUser", "oldmsi");
    static final Setting DB_PWD     = new DefaultSetting("legacyMsiDbPassword", "oldmsi");

    @Inject
    private Logger log;

    @Inject
    MessageService messageService;

    @Inject
    LegacyMessageService legacyMessageService;

    @Inject
    Sequences sequences;

    @Inject
    @Sql("/sql/legacy_messages.sql") String msiSql;

    @Inject
    Settings settings;

    @GET
    public String importMsiWarnings(
            @QueryParam("limit") @DefaultValue("100") int limit,
            @QueryParam("offset") @DefaultValue("0") int offset
    ) {
        log.info("Start importing at most " + limit + " legacy MSI warnings from local DB");
        long t0 = System.currentTimeMillis();
        int count = 0;

        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName(JDBC_DRIVER);

            conn = DriverManager.getConnection(
                    settings.get(DB_URL),
                    settings.get(DB_USER),
                    settings.get(DB_PWD));

            stmt = conn.createStatement();

            String sql = msiSql
                    .replace(":limit", String.valueOf(limit))
                    .replace(":offset", String.valueOf(offset));

            log.info("Executing SQL\n" + sql);
            ResultSet rs = stmt.executeQuery(sql);

            LegacyMessage legacyMessage = null;
            while (rs.next()) {
                Integer id                  = getInt(rs,        "id");
                Integer messageId           = getInt(rs,        "messageId");
                Boolean statusDraft         = getBoolean(rs,    "statusDraft");
                String  navtexNo            = getString(rs,     "navtexNo");
                String  keySubject          = getString(rs,     "keySubject");
                String  amplifyingRemarks   = getString(rs,     "amplifyingRemarks");
                String  amplifyingRemarks2  = getString(rs,     "amplifyingRemarks2");
                Date    issueDate           = getDate(rs,       "issueDate");
                Date    cancellationDate    = getDate(rs,       "cancellationDate");
                Date    created             = getDate(rs,       "created");
                Date    updated             = getDate(rs,       "updated");
                Date    deleted             = getDate(rs,       "deleted");
                String  priority            = getString(rs,     "priority");
                String  messageType         = getString(rs,     "messageType");
                String  specificCategory    = getString(rs,     "specificCategory");
                String  specificLocation    = getString(rs,     "specificLocation");
                String  generalArea         = getString(rs,     "generalArea");
                String  locality            = getString(rs,     "locality");
                String  locationType        = getString(rs,     "locationType");
                Integer pointIndex          = getInt(rs,        "pointIndex");
                Double  pointLatitude       = getDouble(rs,     "pointLatitude");
                Double  pointLongitude      = getDouble(rs,     "pointLongitude");
                Integer pointRadius         = getInt(rs,        "pointRadius");

                if (legacyMessage != null && !legacyMessage.getLegacyId().equals(id)) {
                    saveMessage(legacyMessage, count++);
                    legacyMessage = null;
                }

                if (legacyMessage == null) {
                    // First check if a legacy message with the given id already exists
                    if (legacyMessageService.findByLegacyId(id) != null) {
                        // Skip the import
                        continue;
                    }


                    legacyMessage = new LegacyMessage();
                    legacyMessage.setLegacyId(id);
                    legacyMessage.setNavtexNo(navtexNo);
                    legacyMessage.setVersion(1);

                    NavwarnMessage message = new NavwarnMessage();
                    legacyMessage.setNavwarnMessage(message);

                    MessageSeriesIdentifier identifier = message.getSeriesIdentifier();
                    if (identifier == null) {
                        identifier = new MessageSeriesIdentifier();
                        message.setSeriesIdentifier(identifier);
                        identifier.setNumber((int) sequences.getNextValue(Message.MESSAGE_SEQUENCE));
                        identifier.setAuthority("DMA");
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(issueDate);
                        identifier.setYear(cal.get(Calendar.YEAR));
                        identifier.setType(MessageType.NAVAREA_WARNING);
                    }

                    // Message
                    message.setCreated(created);
                    message.setUpdated(updated);
                    message.setStatus(deleted != null ? MessageStatus.DELETED : (statusDraft ? MessageStatus.DRAFT : MessageStatus.ACTIVE));
                    message.setGeneralArea(generalArea);
                    message.setLocality(StringUtils.defaultString(locality));
                    if (specificLocation != null) {
                        message.getSpecificLocations().add(specificLocation);
                    }
                    message.setIssueDate(issueDate);
                    message.setCancellationDate(cancellationDate);
                    try {
                        message.setPriority(Priority.valueOf(priority));
                    } catch (Exception ex) {
                        message.setPriority(Priority.NONE);
                    }

                    // MessageItem 's
                    MessageItem item1 = new MessageItem();
                    message.getMessageItems().add(item1);

                    MessageCategory cat1 = new MessageCategory();

                    cat1.setGeneralCategory(GeneralCategory.NONE);
                    try {
                        cat1.setSpecificCategory(SpecificCategory.valueOf(specificCategory.toUpperCase().replaceAll(" ", "_")));
                    } catch (Exception ex) {
                        cat1.setSpecificCategory(SpecificCategory.NONE);
                    }

                    cat1.setOtherCategory("");
                    item1.setCategory(cat1);
                    item1.setKeySubject(StringUtils.defaultString(keySubject));
                    item1.setAmplifyingRemarks(StringUtils.defaultString(amplifyingRemarks));

                    if (pointLatitude != null) {
                        MessageLocation.LocationType type;
                        switch (locationType) {
                            case "Point":       type = MessageLocation.LocationType.POINT; break;
                            case "Polygon":     type = MessageLocation.LocationType.POLYGON; break;
                            case "Points":      type = MessageLocation.LocationType.POLYLINE; break;
                            case "Polyline":    type = MessageLocation.LocationType.POLYLINE; break;
                            default:            type = MessageLocation.LocationType.POLYLINE;
                        }
                        MessageLocation loc1 = new MessageLocation(type);
                        if (pointRadius != null) {
                            loc1.setRadius(pointRadius);
                        }
                        item1.getLocations().add(loc1);
                    }
                }

                if (pointLatitude != null) {
                    MessageLocation loc1 =legacyMessage.getNavwarnMessage().getMessageItems().get(0).getLocations().get(0);
                    loc1.addPoint(new Point(pointLatitude, pointLongitude, pointIndex));
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

        return "Imported " + count + " messages";
    }

    private void saveMessage(LegacyMessage legacyMsg, int count) {
        try {
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
