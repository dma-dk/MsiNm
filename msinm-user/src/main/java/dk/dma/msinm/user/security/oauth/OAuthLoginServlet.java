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

import org.slf4j.Logger;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Redirects the requested oauth service, such as Google, Facebook, Linkedin
 *
 * Check: https://github.com/haklop/myqapp/blob/b005df2e100f8aff7c1529097b651b1fd7ce6a4c/src/main/java/com/infoq/myqapp/controller/GoogleController.java
 *
 */
@WebServlet(value = "/oauth/login/*", asyncSupported = true)
public class OAuthLoginServlet extends HttpServlet {

    @Inject
    Logger log;

    @Inject
    OAuthProviders oAuthProviders;

    /**
     * Main GET method
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String providerId = request.getPathInfo().substring(1);
        log.info("Received login request for " + providerId);

        AbstractOAuthProvider provider = oAuthProviders.getProvider(providerId);
        if (provider == null) {
            log.error("No provider exists for " + providerId);
            response.sendRedirect("/");
            return;
        }

        String authUrl = provider.getAuthorizationUrl();
        if (authUrl == null) {
            log.error("Provider " + providerId + " not configured properly");
            response.sendRedirect("/");
            return;
        }

        log.info("Redirecting to " + authUrl);
        response.sendRedirect(authUrl);
    }

}
