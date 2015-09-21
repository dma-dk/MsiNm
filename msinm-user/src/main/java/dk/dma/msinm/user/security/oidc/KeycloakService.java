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
import net.maritimecloud.idreg.client.OIDCClient;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.Reader;
import java.io.StringReader;

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

    private static final String HEADER_ORIGINAL_SCHEME = "originalScheme";

    private static final Setting KEYCLOAK_JSON = new DefaultSetting("keycloakJson", "");

    @Inject
    Logger log;

    @Inject
    Settings settings;

    String keycloakJsonStr = "";
    OIDCClient oidcClient;


    /**
     * Called every minute to check the keycloak status
     */
    @PostConstruct
    @Schedule(persistent=false, second="57", minute="*", hour="*", dayOfWeek="*", year="*")
    @Lock(LockType.WRITE)
    public void checkKeycloakStatus() {
        String previousKeycloakJson = keycloakJsonStr;

        // Refresh the keycloak JSON setting
        keycloakJsonStr = settings.get(KEYCLOAK_JSON);

        // If it has not changed, do nothing
        if (StringUtils.equalsIgnoreCase(keycloakJsonStr, previousKeycloakJson)) {
            return;
        }

        log.info("Keycloak.json definition changed");
        oidcClient = null;
        if (StringUtils.isBlank(keycloakJsonStr)) {
            log.info("Keycloak Service disabled due to blank keycloak.json");
            return;
        }

        try (Reader configFile = new StringReader(keycloakJsonStr)) {

            oidcClient = OIDCClient.newBuilder()
                    .configuration(configFile)
                    .build();

            log.info("Keycloak Service initialized");

        } catch (Exception e) {
            log.error("Error starting Keycloak service. Service not enabled.", e);
        }
    }

    /** Returns if the keycloak service is enabled or not */
    public boolean isEnabled() {
        return oidcClient != null;
    }

    /** Returns the OpenID Connect client if defined, and null otherwise */
    public OIDCClient getOidcClient() {
        return oidcClient;
    }

    /**
     * Returns the base URL of the request
     * <p/>
     * When running HTTPS behind, say, an Apache web server which handles the SSL decoding,
     * then request.getScheme() may still return "http".
     * <p/>
     * If, however, the port-443 VirtualHost is configured to set the header originalScheme=https,
     * then function will ensure that https is used in relative redirects.
     *
     * <p>Example configuration:</p>
     * <pre>
     *     Header add originalScheme "https"
     *     RequestHeader set originalScheme "https"
     * </pre>
     *
     * @param request the request
     * @param appends list of URI components to append
     * @return the base URL + optional appends
     */
    public String getUrl(HttpServletRequest request, String... appends) {
        String scheme = request.getScheme();

        if ("https".equalsIgnoreCase(request.getHeader(HEADER_ORIGINAL_SCHEME))) {
            scheme = "https";
        }

        String result = String.format(
                "%s://%s%s%s",
                scheme,
                request.getServerName(),
                request.getServerPort() == 80 || request.getServerPort() == 443 ? "" : ":" + request.getServerPort(),
                request.getContextPath());
        for (String a : appends) {
            result = result + a;
        }
        return result;
    }
}
