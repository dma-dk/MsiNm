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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import dk.dma.msinm.common.settings.DefaultSetting;
import dk.dma.msinm.common.settings.Setting;
import dk.dma.msinm.common.settings.Settings;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service that starts the Keycloak OpenID Connect Service Client.
 * <p/>
 * The service monitors the "keycloakJson" setting and re-starts the
 * client every time the setting changes.
 * <p/>
 * Code based on org.keycloak.adapters.ServerRequest, ork.keycloak.RSATokenVerifier,
 * org.keycloak.servlet.ServletOAuthClient, etc.
 */
@Singleton
@Startup
@Lock(LockType.READ)
public class KeycloakService {

    private static final Setting KEYCLOAK_JSON = new DefaultSetting("keycloakJson", "");

    private static final String OAUTH_TOKEN_REQUEST_STATE = "OAuth_Token_Request_State";
    private static final String KEYCLOAK_AUTH_TEMPLATE
            = "%s/realms/%s/protocol/openid-connect/auth?response_type=code&client_id=%s&redirect_uri=%s&state=%s";
    private static final String KEYCLOAK_TOKEN_TEMPLATE
            = "%s/realms/%s/protocol/openid-connect/token";

    @Inject
    Logger log;

    @Inject
    Settings settings;

    String keycloakJsonStr = "";
    JsonObject keycloakJson;

    private final AtomicLong counter = new AtomicLong();


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

        log.info("Keycloak JSON definition changed");
        keycloakJson = null;
        try {
            try (JsonReader rdr = Json.createReader(new StringReader(keycloakJsonStr))) {
                keycloakJson = rdr.readObject();
            }
        } catch (Exception e) {
            log.error("Error parsing Keycloak JSON. Service not enabled.");
        }
    }

    /** Returns if the keycloak service is enabled or not */
    public boolean isEnabled() {
        return keycloakJson != null;
    }

    /** URL encodes the given value */
    protected String encode(String val) throws IOException {
        try {
            return URLEncoder.encode(val, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new IOException("Error encoding value " + val, e);
        }
    }

    /** Returns a state code */
    protected String getStateCode() {
        return counter.getAndIncrement() + "/" + UUID.randomUUID().toString();
    }

    /** Redirects to the Keycloak auth server */
    public void redirect(HttpServletResponse response, String callbackUrl) throws IOException {
        JsonObject json = keycloakJson;
        if (json == null) {
            throw new IOException("Keycloak service not enabled");
        }

        String state = getStateCode();
        String url = String.format(
                KEYCLOAK_AUTH_TEMPLATE,
                json.getString("auth-server-url"),
                encode(json.getString("realm")),
                encode(json.getString("resource")),
                encode(callbackUrl),
                encode(state));


        Cookie cookie = new Cookie(OAUTH_TOKEN_REQUEST_STATE, state);
        //cookie.setSecure(isSecure);
        cookie.setPath("/");
        response.addCookie(cookie);
        response.sendRedirect(url);
    }

    /** Utility method used for decoding the public key of the keycloak json */
    public static RSAPublicKey decodePublicKey(String pem) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        pem = pem.replaceAll("-----BEGIN (.*)-----", "")
                .replaceAll("-----END (.*)----", "")
                .replaceAll("\r\n", "")
                .replaceAll("\n", "")
                .trim();
        byte[] der = Base64.getDecoder().decode(pem);
        return (RSAPublicKey)KeyFactory.getInstance("RSA", "BC")
                .generatePublic(new X509EncodedKeySpec(der));
    }

    /** Validate the the 'state' request parameter matches the state cookie */
    protected boolean checkStateParam(HttpServletRequest request) {
        String stateCookie = request.getCookies() == null ? null :
                Arrays.stream(request.getCookies())
                    .filter(c -> c.getName().equals(OAUTH_TOKEN_REQUEST_STATE))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        String stateParam = request.getParameter("state");
        return StringUtils.isNotBlank(stateParam) && stateParam.equals(stateCookie);
    }

    /** Uses the authorization token to get hold of an access token from the Keycloak service */
    public AccessTokenData getBearerToken(HttpServletRequest request, String callbackUrl) throws IOException {

        JsonObject json = keycloakJson;
        if (json == null) {
            throw new IOException("Keycloak service not enabled");
        }

        // Check that the state parameter is correct
        if (!checkStateParam(request)) {
            throw new IOException("Invalid state parameter");
        }

        String code = request.getParameter("code");
        if (code == null) {
            throw new IOException("code parameter was null");
        }

        String url = String.format(
                KEYCLOAK_TOKEN_TEMPLATE,
                json.getString("auth-server-url"),
                encode(json.getString("realm")));

        String secret = json.getString("resource") + ":" + json.getJsonObject("credentials").getString("secret");
        String header = "Basic " + Base64.getEncoder().encodeToString(secret.getBytes("UTF-8"));

        Client client = ClientBuilder.newClient();

        Form form = new Form();
        form.param("grant_type", "authorization_code")
                .param("code", code)
                .param("redirect_uri", callbackUrl);

        Response response = client.target(url)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", header)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        if (response.getStatus() != 200) {
            throw new IOException("Error getting authorization token");
        }
        String authToken = response.readEntity(AccessTokenResponse.class).getToken();

        try {
            SignedJWT jwt = SignedJWT.parse(authToken);
            JWSVerifier verifier = new RSASSAVerifier(decodePublicKey(json.getString("realm-public-key")));

            if (!jwt.verify(verifier)) {
                throw new IOException("Access token has invalid signature");
            }
            // TODO: Check realm, issuer, expiry?

            return AccessTokenData.parseJWTToken(jwt, json.getString("resource"));

        } catch (Exception e) {
            throw new IOException("Access token error", e);
        }
    }


    // ******** Helper Classes **********

    /** Used for reading an encoded access token */
    @JsonIgnoreProperties(ignoreUnknown=true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class AccessTokenResponse {
        @XmlElement(name = "access_token", required = true)
        protected String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

}
