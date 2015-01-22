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
package dk.dma.msinm.user.security.oauth;

import dk.dma.msinm.common.MsiNmApp;
import dk.dma.msinm.common.settings.Settings;
import dk.dma.msinm.user.User;
import dk.dma.msinm.user.UserService;
import org.apache.commons.lang.StringUtils;
import org.scribe.builder.api.Api;
import org.scribe.model.OAuthConstants;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.http.HttpServletRequest;
import java.io.StringReader;
import java.util.Base64;

/**
 * A Google specific OAuth provider.
 */
@Singleton
@Lock(LockType.READ)
@Startup
public class GoogleOAuthProvider extends AbstractOAuthProvider {

    @Inject
    Logger log;

    @Inject
    MsiNmApp app;

    @Inject
    Settings settings;

    @Inject
    OAuthProviders oAuthProviders;

    @Inject
    UserService userService;

    /**
     * Upon startup, register this provider with the OAuthProviders
     */
    @PostConstruct
    public void init() {
        oAuthProviders.registerProvider(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOAuthProviderId() {
        return "google";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends Api> getOAuthApi() {
        return GoogleApiProvider.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOAuthScope() {
        return "openid profile email";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAuthorizationUrl() {

        OAuthService service = getOAuthService(settings, app.getBaseUri());
        if (service == null) {
            log.warn("OAuth service not available for " + getOAuthProviderId());
            return null;
        }

        //Token requestToken = service.getRequestToken();
        return service.getAuthorizationUrl(OAuthConstants.EMPTY_TOKEN);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User authenticateUser(HttpServletRequest request) throws Exception {

        OAuthService service = getOAuthService(settings, app.getBaseUri());
        if (service == null) {
            log.warn("OAuth service not available for " + getOAuthProviderId());
            throw new Exception("OAuth service not available for " + getOAuthProviderId());
        }

        String oauthVerifier = request.getParameter("code");
        Verifier verifier = new Verifier(oauthVerifier);

        Token accessToken = service.getAccessToken(OAuthConstants.EMPTY_TOKEN, verifier);
        log.info("Access Granted to Google with token " + accessToken);

        // check
        // https://github.com/haklop/myqapp/blob/b005df2e100f8aff7c1529097b651b1fd7ce6a4c/src/main/java/com/infoq/myqapp/controller/GoogleController.java
        String idToken = GoogleApiProvider.getIdToken(accessToken.getRawResponse());
        String userIdToken = idToken.split("\\.")[1];
        log.info("Received ID token " + userIdToken);

        String profile = new String(Base64.getDecoder().decode(userIdToken));
        log.info("Decoded " + profile);

        try (JsonReader jsonReader = Json.createReader(new StringReader(profile))) {
            JsonObject json = jsonReader.readObject();

            String email = json.containsKey("email") ? json.getString("email") : null;
            String firstName = json.containsKey("given_name") ? json.getString("given_name") : null;
            String lastName = json.containsKey("family_name") ? json.getString("family_name") : null;
            if (StringUtils.isBlank(email)) {
                throw new Exception("No email found in OAuth token");
            }
            log.info("Email  " + email);

            User user = userService.findByEmail(email);
            if (user == null) {
                user = new User();
                user.setEmail(email);
                user.setLanguage("en");
                user.setFirstName(firstName);
                user.setLastName(StringUtils.isBlank(lastName) ? email : lastName);
                user = userService.registerOAuthOnlyUser(user);
            }
            return user;
        }
    }
}
