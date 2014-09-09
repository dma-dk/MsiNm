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

import dk.dma.msinm.common.settings.DefaultSetting;
import dk.dma.msinm.common.settings.Setting;
import dk.dma.msinm.common.settings.Settings;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Defines the interface to the Danish MSI database
 */
public class LegacyDatabase {

    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final Setting DB_URL     = new DefaultSetting("legacyMsiDbUrl", "jdbc:mysql://localhost:3306/oldmsi");
    static final Setting DB_USER    = new DefaultSetting("legacyMsiDbUser", "oldmsi");
    static final Setting DB_PWD     = new DefaultSetting("legacyMsiDbPassword", "oldmsi");


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
     * Returns a new connection to the legacy database.
     * Important to close the connection afterwards.
     * @return a new connection to the legacy database.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                settings.get(DB_URL),
                settings.get(DB_USER),
                settings.get(DB_PWD));
    }


    public static String getString(ResultSet rs, String key) throws SQLException {
        String val = rs.getString(key);
        return rs.wasNull() ? null : val;
    }

    public static Integer getInt(ResultSet rs, String key) throws SQLException {
        Integer val = rs.getInt(key);
        return rs.wasNull() ? null : val;
    }

    public static Double getDouble(ResultSet rs, String key) throws SQLException {
        Double val = rs.getDouble(key);
        return rs.wasNull() ? null : val;
    }

    public static Date getDate(ResultSet rs, String key) throws SQLException {
        Timestamp val = rs.getTimestamp(key);
        return rs.wasNull() ? null : val;
    }


    public static Boolean getBoolean(ResultSet rs, String key) throws SQLException {
        boolean val = rs.getBoolean(key);
        return rs.wasNull() ? null : val;
    }

}
