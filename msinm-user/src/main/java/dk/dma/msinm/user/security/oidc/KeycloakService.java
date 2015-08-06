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
package dk.dma.msinm.user.security.oidc;

import dk.dma.msinm.common.settings.DefaultSetting;
import dk.dma.msinm.common.settings.Setting;
import dk.dma.msinm.common.settings.Settings;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.servlet.ServletOAuthClient;
import org.keycloak.servlet.ServletOAuthClientBuilder;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.io.IOException;

/**
 * Service that starts the Keycloak OpenID Connect Service Client.
 * <p/>
 * The service monitors the "keycloakJson" setting and re-starts the
 * client every time the setting changes.
 */
@Singleton
@Startup
@Lock(LockType.READ)
public class KeycloakService {

    private static final Setting KEYCLOAK_JSON = new DefaultSetting("keycloakJson", "");

    @Inject
    Logger log;

    @Inject
    Settings settings;

    String keycloakJson = "";

    ServletOAuthClient keycloakClient;

    KeycloakDeployment keycloakDeployment;

    /**
     * Called every minute to check the keycloak status
     */
    @PostConstruct
    @Schedule(persistent=false, second="57", minute="*", hour="*", dayOfWeek="*", year="*")
    @Lock(LockType.WRITE)
    public void checkKeycloakStatus() {
        String previousKeycloakJson = keycloakJson;

        // Refresh the keycloak JSON setting
        keycloakJson = settings.get(KEYCLOAK_JSON);

        // If it has not changed, do nothing
        if (StringUtils.equalsIgnoreCase(keycloakJson, previousKeycloakJson)) {
            return;
        }

        start();
    }

    /** Attempts to start the keycloak client */
    @Lock(LockType.WRITE)
    private void start() {
        // Stop any existing client
        stop();

        if (StringUtils.isNotBlank(keycloakJson)) {
            log.info("Starting keycloak service");

            try {
                keycloakDeployment = KeycloakDeploymentBuilder.build(IOUtils.toInputStream(keycloakJson, "UTF-8"));

                keycloakClient = new ServletOAuthClient();
                ServletOAuthClientBuilder.build(IOUtils.toInputStream(keycloakJson, "UTF-8"), keycloakClient);
                keycloakClient.start();

                log.info("Keycloak service started");
            } catch (IOException e) {
                log.error("Error starting keycloak service", e);
                stop();
            }
        }
    }

    /** Attempts to stop the keycloak client */
    @PreDestroy
    @Lock(LockType.WRITE)
    private void stop() {
        if (keycloakClient != null) {
            log.info("Stopping keycloak service");
            try {
                keycloakClient.stop();
            } catch (Exception ex) {
                log.error("Error stopping keycloak client");
            }
            keycloakClient = null;
        }
        if (keycloakDeployment != null) {
            keycloakDeployment = null;
        }
    }

    /** Returns if the keycloak service has been started */
    public boolean isStated() {
        return keycloakClient != null;
    }

    // ******** Getters **********

    public ServletOAuthClient getKeycloakClient() {
        return keycloakClient;
    }

    public KeycloakDeployment getKeycloakDeployment() {
        return keycloakDeployment;
    }
}
