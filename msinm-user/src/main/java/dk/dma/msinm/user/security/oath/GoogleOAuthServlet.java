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
package dk.dma.msinm.user.security.oath;

import dk.dma.msinm.common.settings.DefaultSetting;
import dk.dma.msinm.common.settings.Setting;
import dk.dma.msinm.common.settings.Settings;
import dk.dma.msinm.user.User;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.GoogleApi20;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * A google implementation of the OAuth service.
 * <p>
 * Based largely on: http://www.literak.cz/OAuthLogin/
 */
@WebServlet(value = "/oauth/google/*")
public class GoogleOAuthServlet extends BaseOAuthServlet {

    private static final String PROVIDER = "google";
    private static final String RESOURCE_URL = "https://www.googleapis.com/plus/v1/people/me";

    private static final Setting OAUTH_API_KEY = new DefaultSetting("oauthGoogleApiKey", "959114872597-87n65nunkr6hm8qf3japiighcb0p15q2.apps.googleusercontent.com");
    private static final Setting OAUTH_API_SECRET = new DefaultSetting("oauthGoogleApiSecret", "-g_wyNtY6bQM65iIDhv-4WRl");

    @Inject
    private Logger log;

    @Inject
    Settings settings;

    @Override
    protected OAuthProvider getProvider() {
        OAuthProvider provider = new OAuthProvider(PROVIDER);

        provider.setDbKey("GG");
        provider.setApiKey(settings.get(OAUTH_API_KEY));
        provider.setApiSecret(settings.get(OAUTH_API_SECRET));

        return provider;
    }

    @Override
    protected OAuthService getService(HttpServletRequest request, OAuthProvider provider) {
        String secret = new BigInteger(130, new SecureRandom()).toString(32);
        request.getSession().setAttribute(KEY_STATE, secret);
        return new ServiceBuilder()
                 //.provider(Google2API.class)
                .provider(GoogleApi20.class)
                .apiKey(provider.getApiKey())
                .apiSecret(provider.getApiSecret())
                .scope("openid profile email")
                .grantType("authorization_code")
                 // .state(secret) TODO uncomment when subscribe 2.1 is releases
                 // Google checks if callback URL is registered in app settings
                .callback(getCallbackUrl(request, provider.getUriKey()))
                //.debug()
                .build();
    }

    @Override
    protected void handleHandshake(HttpServletRequest request, HttpServletResponse response, OAuthProvider provider) throws IOException {
        handleHandshakeOAuth2(request, response, provider);
    }

    @Override
    protected boolean handleCallback(HttpServletRequest request, HttpServletResponse response, OAuthProvider provider) throws ServletException, IOException {
        String verifierValue = request.getParameter("code");
        if (verifierValue == null) {
            log.warn("Callback did not receive code parameter!");
            return false;
        }
        Verifier verifier = new Verifier(verifierValue);

        OAuthService service = getService(request, provider);
        Token accessToken = service.getAccessToken(NULL_TOKEN, verifier);
        //        GoogleToken accessToken = (GoogleToken) service.getAccessToken(NULL_TOKEN, verifier); TODO subscribe 2.1
        //        String screenName = (String) accessToken.idToken.get("sub");
        String screenName = null;

        HttpSession session = request.getSession();
        OAuthLogin oAuthLogin = findExistingUser(screenName, accessToken.getToken(), provider);
        if (oAuthLogin != null) {
            log.info("Access token matched, oAuth.id = " + oAuthLogin.getId());
            session.setAttribute(KEY_USER, oAuthLogin.getUser());
            return true;
        }

        OAuthRequest resourceRequest = new OAuthRequest(Verb.GET, RESOURCE_URL);
        service.signRequest(accessToken, resourceRequest);
        Response resourceResponse = resourceRequest.send();

        JsonObject jsonObject = Json.createReader(new StringReader(resourceResponse.getBody())).readObject();

        User user = (User) session.getAttribute(KEY_USER);
        if (user == null) {
            user = new User(jsonObject.containsKey("displayName") ? jsonObject.getString("displayName") : "N/A");

            // todo remove when subscribe 2.1
            screenName = jsonObject.getString("id");

            JsonObject jsonObjectName = (JsonObject)jsonObject.get("name");
            if (jsonObjectName.containsKey("givenName")) {
                user.setFirstName(jsonObjectName.getString("givenName"));
            }

            if (jsonObjectName.containsKey("familyName")) {
                user.setLastName(jsonObjectName.getString("familyName"));
            }

            if (jsonObject.containsKey("emails")) {

                JsonArray emails = jsonObject.getJsonArray("emails");
                if (! emails.isEmpty()) {
                    jsonObject = (JsonObject)emails.get(0);
                    user.setEmail(jsonObject.getString("value"));
                }
            }

            userService.saveEntity(user);
            session.setAttribute(KEY_USER, user);
            log.debug("Created user, id " + user.getId());
        }

        oAuthLogin = new OAuthLogin(user, provider.getDbKey(), screenName);
        oAuthLogin.setAccessToken(accessToken.getToken());
        userService.saveEntity(oAuthLogin);
        log.debug("Created oAuthLogin, id = " + oAuthLogin.getId() + ", user = " + user.getId());
        return true;
    }
}
