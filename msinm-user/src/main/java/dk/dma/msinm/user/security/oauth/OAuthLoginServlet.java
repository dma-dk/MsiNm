package dk.dma.msinm.user.security.oauth;

import dk.dma.msinm.common.MsiNmApp;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.GoogleApi;
import org.scribe.model.OAuthConstants;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;
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
    MsiNmApp app;

    /**
     * Main GET method
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        log.info("Received login request from " + request.getPathInfo());

        OAuthService service = new ServiceBuilder()
                .provider(GoogleApiProvider.class)
                .apiKey("959114872597-87n65nunkr6hm8qf3japiighcb0p15q2.apps.googleusercontent.com")
                .apiSecret("-g_wyNtY6bQM65iIDhv-4WRl")
                .scope("openid profile email")
                .callback(app.getBaseUri() + "/oauth/callback/google")
                .build();

        //Token requestToken = service.getRequestToken();
        String authUrl = service.getAuthorizationUrl(OAuthConstants.EMPTY_TOKEN);
        log.info("Redirecting to " + authUrl);

        // TODO: For now we just store the token in the session
        //request.getSession().setAttribute("googleOauthRequestToken", requestToken);

        response.sendRedirect(authUrl);
    }

}
