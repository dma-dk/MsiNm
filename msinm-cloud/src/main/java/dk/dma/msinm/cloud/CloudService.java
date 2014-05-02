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
package dk.dma.msinm.cloud;

import dk.dma.msinm.common.settings.DefaultSetting;
import dk.dma.msinm.common.settings.Setting;
import dk.dma.msinm.common.settings.Settings;
import net.maritimecloud.core.id.MaritimeId;
import net.maritimecloud.core.id.MmsiId;
import net.maritimecloud.net.ClosingCode;
import net.maritimecloud.net.MaritimeCloudClient;
import net.maritimecloud.net.MaritimeCloudClientConfiguration;
import net.maritimecloud.net.MaritimeCloudConnection;
import net.maritimecloud.util.geometry.Position;
import net.maritimecloud.util.geometry.PositionReader;
import net.maritimecloud.util.geometry.PositionTime;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Schedule;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * Main interface to the Maritime Cloud service
 */
@Singleton
@Startup
public class CloudService {

    private final static Setting CLOUD_HOST     = new DefaultSetting("cloudHost", "mms.maritimecloud.net:43234");
    private final static Setting CLOUD_ID       = new DefaultSetting("cloudId", "999000007");
    private final static Setting CLOUD_POS      = new DefaultSetting("cloudLatLonPos", "55.6546523 12.5144583");
    private final static Setting CLOUD_DEBUG    = new DefaultSetting("cloudDebug", "true");

    @Inject
    private Logger log;

    @Inject
    Settings settings;

    MaritimeCloudClient connection;

    String host;
    MaritimeId maritimeId;
    Position position;
    boolean debug;

    /**
     * Read the configuration and start the cloud connection
     */
    @PostConstruct
    public void init() {
        readSettings();
        initCloudConnection();
    }

    /**
     * Stop the Cloud connection
     */
    @PreDestroy
    public void closeConnection() {
        try {
            connection.close();
            connection.awaitTermination(2, TimeUnit.SECONDS);
            log.info("Closed cloud connection");
        } catch (Exception e) {
            log.error("Error terminating cloud connection");
        }
    }

    /**
     * Called every 10 seconds to check the cloud status
     */
    @Schedule(persistent=false, second="*/10", minute="*", hour="*", dayOfWeek="*", year="*")
    public void checkCloudStatus() {
        if (connection == null) {
            initCloudConnection();
        }
    }

    /**
     * (Re-)reads the settings
     */
    void readSettings() {
        debug = settings.getBoolean(CLOUD_DEBUG);
        host = settings.get(CLOUD_HOST);
        maritimeId = new MmsiId((int)settings.getLong(CLOUD_ID));
        String[] latLon = settings.get(CLOUD_POS).split(" ");
        position = PositionTime.create(Double.valueOf(latLon[0]), Double.valueOf(latLon[1]));

        log.info(String.format("Read cloud settings: host=%s, id=%s, pos=%s", host, maritimeId, position));
    }

    /**
     * Initialize the Maritime Cloud connection
     * @return success or failure
     */
    private boolean initCloudConnection() {
        log.info("Connecting to cloud server: " + host + " with maritime id " + maritimeId);

        MaritimeCloudClientConfiguration enavCloudConnection = MaritimeCloudClientConfiguration.create(maritimeId);

        // Hook up a position reader
        enavCloudConnection.setPositionReader(new PositionReader() {
            @Override
            public PositionTime getCurrentPosition() {
                return PositionTime.create(
                        position.getLatitude(),
                        position.getLongitude(),
                        System.currentTimeMillis());
            }});

        // Check if we need to log the MaritimeCloudConnection activity
        enavCloudConnection.addListener(new MaritimeCloudConnection.Listener() {
            @Override
            public void messageReceived(String message) {
                if (debug) {
                    log.info("Received:" + message);
                }
            }

            @Override
            public void messageSend(String message) {
                if (debug) {
                    log.info("Sending :" + message);
                }
            }

            @Override
            public void connecting(URI host) {
                if (debug) {
                    log.info("Connecting to cloud :" + host);
                }
            }

            @Override
            public void disconnected(ClosingCode closeReason) {
                if (debug) {
                    log.info("Disconnecting from cloud :" + closeReason);
                }
            }
        });

        try {
            enavCloudConnection.setHost(host);
            connection = enavCloudConnection.build();

            if (connection != null) {
                log.info("Connected succesfully to cloud server: " + host + " with shipId " + maritimeId);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("Failed to connect to server: " + e);
            return false;
        }
    }
}
