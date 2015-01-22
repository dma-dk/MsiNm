package dk.dma.msinm.user.security.oauth;

import dk.dma.msinm.common.MsiNmApp;
import dk.dma.msinm.common.util.WebUtils;
import dk.dma.msinm.user.User;
import dk.dma.msinm.user.security.AuthCache;
import dk.dma.msinm.user.security.JWTService;
import dk.dma.msinm.user.security.JWTToken;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Serves as an endpoint for external oauth authentication
 */
@WebServlet(value = "/oauth/callback/*", asyncSupported = true)
public class OAuthCallbackServlet extends HttpServlet {

    @Inject
    Logger log;

    @Inject
    OAuthProviders oAuthProviders;

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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        try {
            String providerId = request.getPathInfo().substring(1);
            log.info("Received callback request from " + providerId);

            AbstractOAuthProvider provider = oAuthProviders.getProvider(providerId);
            if (provider == null) {
                log.error("No provider exists for " + providerId);
                response.sendRedirect("/");
                return;
            }

            // Look up or create the authenticated user
            User user = provider.authenticateUser(request);

            // Create a new JWT token
            JWTToken jwt = jwtService.createSignedJWT(getJwtIssuer(request), user);
            String token = jwtService.createTempJwtPwdToken(AuthCache.AUTH_TOKEN_PREFIX);

            // Cache it for at most 1 minute in the auth cache
            authCache.getCache().put(token, jwt);
            log.info("Setting auth token " + token + " to jwt token " + jwt);

            // Redirect to the auth token landing page, which will use the token to authenticate
            WebUtils.nocache(response).sendRedirect("/index.html#/auth/" + token);
            return;


        } catch (Exception e) {
            log.error("Error authentication user via OAuth ", e);
        }

        response.sendRedirect("/");
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
