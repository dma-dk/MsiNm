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

import dk.dma.msinm.common.MsiNmApp;
import dk.dma.msinm.common.util.WebUtils;
import dk.dma.msinm.user.User;
import dk.dma.msinm.user.UserService;
import dk.dma.msinm.user.security.AuthCache;
import dk.dma.msinm.user.security.JWTService;
import dk.dma.msinm.user.security.JWTToken;
import net.maritimecloud.idreg.client.AccessTokenData;
import net.maritimecloud.idreg.client.AuthErrorException;
import net.maritimecloud.idreg.client.OIDCUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * Called by the Keycloak OpenID Connect service when the user has attempted to log in
 */
@WebServlet(value = "/oidc-callback", asyncSupported = true)
public class KeycloakCallbackServlet extends HttpServlet {

    @Inject
    Logger log;

    @Inject
    KeycloakService keycloakService;

    @Inject
    UserService userService;

    @Inject
    MsiNmApp app;

    @Inject
    JWTService jwtService;

    @Inject
    AuthCache authCache;

    /**
     * Main GET method
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        // Check if the service is enabled
        if (!keycloakService.isEnabled()) {
            log.warn("The OpenID Connect service is not enabled");
            response.sendRedirect("/");
            return;
        }

        log.info("OpenID Connect callback called");
        try {
            OIDCUtils.nocache(response);
            String callbackUrl = OIDCUtils.getUrl(request, "/oidc-callback");
            AccessTokenData accessTokenData = keycloakService.getOidcClient().handleAuthServerCallback(request, callbackUrl);
            log.info("OpenID Connect authentication success: " + accessTokenData);

            // Look up or create the authenticated user
            User user = authenticateUser(accessTokenData);

            // Create a new JWT token
            JWTToken jwt = jwtService.createSignedJWT(getJwtIssuer(request), user);
            String token = jwtService.createTempJwtPwdToken(AuthCache.AUTH_TOKEN_PREFIX);

            // Cache it for at most 1 minute in the auth cache
            authCache.getCache().put(token, jwt);
            log.info("Setting auth token " + token + " to jwt token " + jwt);

            // Redirect to the auth token landing page, which will use the token to authenticate
            WebUtils.nocache(response).sendRedirect("/index.html#/auth/" + token);
            return;

        } catch (AuthErrorException e) {
            log.error("OpenID Connect authentication error", e);
        } catch (Exception e) {
            log.error("Error logging in user", e);
        }
        response.sendRedirect("/");
    }


    /**
     * Lookup or create a user from an OpenID Connect access token
     * @param accessToken the access token
     * @return the user
     * @throws Exception in case of an error
     */
    private User authenticateUser(AccessTokenData accessToken) throws Exception {
        if (StringUtils.isBlank(accessToken.getEmail())) {
            throw new Exception("No email found in OAuth token");
        }

        Set<String> roles = accessToken.getResourceRoles().size() > 0
                ? accessToken.getResourceRoles()
                : accessToken.getRealmRoles();
        log.info("Logged in user " + accessToken.getEmail() + " with roles " + roles);

        // Look up or create the user
        User user = userService.findByEmail(accessToken.getEmail());
        if (user == null) {
            user = new User();
            user.setEmail(accessToken.getEmail());
            user.setLanguage("en");
            user.setFirstName(accessToken.getGivenName());
            user.setLastName(StringUtils.isBlank(accessToken.getFamilyName()) ? accessToken.getEmail() : accessToken.getFamilyName());
            user = userService.registerOAuthOnlyUser(user);
        }

        // Update the user roles
        user = userService.checkUpdateRoles(user, roles.toArray(new String[roles.size()]));

        return user;
    }

    /**
     * Returns the JWT issuer based on the current server name
     * @param request the servlet request
     * @return the JWT issuer
     */
    protected String getJwtIssuer(HttpServletRequest request) {
        return String.format("%s://%s", request.getScheme(), request.getServerName());
    }

}
