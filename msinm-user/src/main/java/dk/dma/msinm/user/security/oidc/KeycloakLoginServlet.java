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

import net.e2.bw.idreg.client.OIDCUtils;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Use the Keycloak OpenID Connect service to log the user in
 */
@WebServlet(value = "/oidc-login", asyncSupported = true)
public class KeycloakLoginServlet extends HttpServlet {

    @Inject
    Logger log;

    @Inject
    KeycloakService keycloakService;

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

        log.info("OpenID Connect login called");
        OIDCUtils.nocache(response);
        String callbackUrl = keycloakService.getUrl(request, "/oidc-callback");
        keycloakService.getOidcClient().redirectToAuthServer(response, callbackUrl);
    }
}
