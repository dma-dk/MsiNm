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
