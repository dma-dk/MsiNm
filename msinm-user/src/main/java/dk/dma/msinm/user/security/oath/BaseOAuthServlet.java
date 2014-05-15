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

import dk.dma.msinm.common.web.WebUtils;
import dk.dma.msinm.user.UserService;
import org.slf4j.Logger;

import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Super class for OAuth processors
 * <p>
 * Based largely on: http://www.literak.cz/OAuthLogin/
 */
public abstract class BaseOAuthServlet extends HttpServlet  {

    public static final String KEY_TOKEN = "OAUTH_TOKEN";
    public static final String KEY_STATE = "OAUTH_STATE";
    public static final String KEY_USER = "USER";

    protected static final Token NULL_TOKEN = null;

    @Inject
    private Logger log;

    @Inject
    UserService userService;


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            OAuthProvider provider = getProvider();
            if (provider == null) {
                log.error("Unexpected provider for " + request.getPathInfo());
                throw new ServletException("Missing configuration for OAuth provider '" + request.getPathInfo() + "'");
            }

            String pathInfo = request.getPathInfo();
            if (pathInfo != null && pathInfo.endsWith("/handshake")) {
                executeHandshake(provider, request, response);
            } else if (pathInfo != null && pathInfo.endsWith("/callback")) {
                executeCallback(provider, request, response);
            } else {
                response.sendError(404);
                return;
            }


        } catch (Exception e) {
            log.error("OAuth processing failed!", e);
            throw new ServletException("OAuth processing failed!", e);
        }
    }

    protected abstract OAuthProvider getProvider();

    protected abstract OAuthService getService(HttpServletRequest request, OAuthProvider provider);

    protected abstract void handleHandshake(HttpServletRequest request, HttpServletResponse response, OAuthProvider provider) throws IOException;

    protected abstract boolean handleCallback(HttpServletRequest request, HttpServletResponse response, OAuthProvider provider) throws ServletException, IOException;

    protected void executeHandshake(OAuthProvider params, HttpServletRequest request,
                                   HttpServletResponse response) throws ServletException, IOException {
        try {
            handleHandshake(request, response, params);
        } catch (Exception e) {
            log.error("Handshake processing failed for " + params.uriKey, e);
            throw new ServletException(e);
        }
    }

    protected void executeCallback(OAuthProvider params, HttpServletRequest request,
                           HttpServletResponse response) throws ServletException, IOException {
        try {
            boolean logged = handleCallback(request, response, params);
            if (logged) {
                response.sendRedirect(request.getContextPath() + "/logged.jsp");
            } else {
                response.sendRedirect(request.getContextPath() + "/login.jsp");
            }
        } catch (ServletException e) {
            log.error("Callback processing failed for " + params.uriKey, e);
            throw e;
        } catch (Exception e) {
            log.error("Callback processing failed for " + params.uriKey, e);
            throw new ServletException(e);
        }
    }


    protected void handleHandshakeOAuth1(HttpServletRequest request, HttpServletResponse response, OAuthProvider provider) throws IOException {
        OAuthService service = getService(request, provider);
        Token requestToken = service.getRequestToken();
        HttpSession session = request.getSession();
        session.setAttribute(KEY_TOKEN, requestToken);
        String redirectUrl = service.getAuthorizationUrl(requestToken);
        response.sendRedirect(redirectUrl);
    }

    protected void handleHandshakeOAuth2(HttpServletRequest request, HttpServletResponse response, OAuthProvider provider) throws IOException {
        OAuthService service = getService(request, provider);
        String redirectUrl = service.getAuthorizationUrl(NULL_TOKEN);
        response.sendRedirect(redirectUrl);
    }

    protected OAuthLogin findExistingUser(String login, String accessToken, OAuthProvider provider) {
        OAuthLogin oAuthLogin;
        if (login != null) {
            oAuthLogin = userService.findByProvider(provider.getDbKey(), login);
            if (oAuthLogin == null) {
                return null;
            }

            if (accessToken == null || !accessToken.equals(oAuthLogin.getAccessToken())) {
                log.debug("Updating access token, oAuth.id = " + oAuthLogin.getId());
                oAuthLogin.setAccessToken(accessToken);
                userService.saveEntity(oAuthLogin);
            }
            return oAuthLogin;
        }

        if (accessToken != null) {
            return userService.findByAccessToken(provider.getDbKey(), accessToken);
        }

        return null;
    }

    protected String getCallbackUrl(HttpServletRequest request, String uriKey) {
        return WebUtils.getBaseUrl(request, request.getServletPath(), "/callback");
    }

}
