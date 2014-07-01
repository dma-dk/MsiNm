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

import dk.dma.msinm.common.db.Sql;
import dk.dma.msinm.common.sequence.Sequences;
import dk.dma.msinm.common.settings.DefaultSetting;
import dk.dma.msinm.common.settings.Setting;
import dk.dma.msinm.common.settings.Settings;
import dk.dma.msinm.legacy.msi.model.LegacyMessage;
import dk.dma.msinm.model.Area;
import dk.dma.msinm.model.Category;
import dk.dma.msinm.model.Location;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.model.MessageDesc;
import dk.dma.msinm.model.SeriesIdentifier;
import dk.dma.msinm.model.Status;
import dk.dma.msinm.model.Type;
import dk.dma.msinm.model.Point;
import dk.dma.msinm.model.Priority;
import dk.dma.msinm.service.AreaService;
import dk.dma.msinm.service.CategoryService;
import dk.dma.msinm.service.MessageService;
import org.apache.commons.lang.StringUtils;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.slf4j.Logger;

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
 * Imports data from a local db dump of the Danish MSI database
 */
@Path("/import/legacy-db-msi")
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
    AreaService areaService;

    @Inject
    CategoryService categoryService;

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

                    Message message = new Message();
                    legacyMessage.setMessage(message);

                    SeriesIdentifier identifier = message.getSeriesIdentifier();
                    if (identifier == null) {
                        identifier = new SeriesIdentifier();
                        message.setSeriesIdentifier(identifier);
                        identifier.setNumber((int) sequences.getNextValue(Message.MESSAGE_SEQUENCE));
                        identifier.setAuthority("DMA");
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(validFrom);
                        identifier.setYear(cal.get(Calendar.YEAR));
                    }

                    // Message
                    message.setCreated(created);
                    message.setUpdated(updated);
                    message.setType(Type.NAVAREA_WARNING);
                    message.setStatus(deleted != null ? Status.DELETED : (statusDraft ? Status.DRAFT : Status.ACTIVE));
                    message.setValidFrom(validFrom);
                    message.setValidTo(validTo);
                    try {
                        message.setPriority(Priority.valueOf(priority));
                    } catch (Exception ex) {
                        message.setPriority(Priority.NONE);
                    }

                    // Message Desc
                    MessageDesc descEn = message.createDesc("en");
                    descEn.setTitle(StringUtils.defaultString(title, descriptionEn));
                    descEn.setDescription(descriptionEn);
                    descEn.setVicinity(area3En);

                    MessageDesc descDa = message.createDesc("da");
                    descDa.setTitle(StringUtils.defaultString(title, descriptionDa));
                    descDa.setDescription(descriptionDa);
                    descDa.setVicinity(area3Da);

                    // Areas
                    Area area = findOrCreateArea(area1En, area1Da, null);
                    area = findOrCreateArea(area2En, area2Da, area);
                    message.setArea(area);

                    // Categories
                    Category category = findOrCreateCategory(category1En, category1Da, null);
                    category = findOrCreateCategory(category2En, category2Da, category);
                    message.getCategories().add(category);

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
                    Location loc1 =legacyMessage.getMessage().getLocations().get(0);
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

        return "Imported " + count + " messages";
    }

    private Area findOrCreateArea(String nameEn, String nameDa, Area parent) {
        Integer parentId = (parent == null) ? null : parent.getId();

        if (StringUtils.isNotBlank(nameEn) || StringUtils.isNotBlank(nameDa)) {
            Area area = areaService.findByName(nameEn, "en", parentId);
            if (area == null) {
                area = areaService.findByName(nameDa, "da", parentId);
            }
            if (area == null) {
                area = new Area();
                if (StringUtils.isNotBlank(nameEn)) {
                    area.createDesc("en").setName(nameEn);
                }
                if (StringUtils.isNotBlank(nameDa)) {
                    area.createDesc("da").setName(nameDa);
                }
                area = areaService.createArea(area, parentId);
            }
            return area;
        }
        return parent;
    }

    private Category findOrCreateCategory(String nameEn, String nameDa, Category parent) {
        Integer parentId = (parent == null) ? null : parent.getId();

        if (StringUtils.isNotBlank(nameEn) || StringUtils.isNotBlank(nameDa)) {
            Category category = categoryService.findByName(nameEn, "en", parentId);
            if (category == null) {
                category = categoryService.findByName(nameDa, "da", parentId);
            }
            if (category == null) {
                category = new Category();
                if (StringUtils.isNotBlank(nameEn)) {
                    category.createDesc("en").setName(nameEn);
                }
                if (StringUtils.isNotBlank(nameDa)) {
                    category.createDesc("da").setName(nameDa);
                }
                category = categoryService.createCategory(category, parentId);
            }
            return category;
        }
        return parent;
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
