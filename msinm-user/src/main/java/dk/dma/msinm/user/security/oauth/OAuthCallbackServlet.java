package dk.dma.msinm.user.security.oauth;

import dk.dma.msinm.common.MsiNmApp;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthConstants;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

/**
 * Serves as an endpoint for external oauth authentication
 */
@WebServlet(value = "/oauth/callback/*", asyncSupported = true)
public class OAuthCallbackServlet extends HttpServlet {

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

        log.info("Received callback request from " + request.getPathInfo());
        request.getParameterMap().forEach((k,v) -> log.info("Param " + k + " = " + Arrays.asList(v)));

        String oauthVerifier = request.getParameter("code");
        Verifier verifier = new Verifier(oauthVerifier);

        OAuthService service = new ServiceBuilder()
                .provider(GoogleApiProvider.class)
                .apiKey("959114872597-87n65nunkr6hm8qf3japiighcb0p15q2.apps.googleusercontent.com")
                .apiSecret("-g_wyNtY6bQM65iIDhv-4WRl")
                .scope("openid email")
                .callback(app.getBaseUri() + "/oauth/callback/google")
                .build();

        //Token requestToken = (Token)request.getSession().getAttribute("googleOauthRequestToken");
        Token accessToken = service.getAccessToken(OAuthConstants.EMPTY_TOKEN, verifier);
        log.info("Access Granted to Google with token " + accessToken);

        // check
        // https://github.com/haklop/myqapp/blob/b005df2e100f8aff7c1529097b651b1fd7ce6a4c/src/main/java/com/infoq/myqapp/controller/GoogleController.java
        String idToken = GoogleApiProvider.getIdToken(accessToken.getRawResponse());
        String userIdToken = idToken.split("\\.")[1];
        log.info("Received ID token " + userIdToken);
        log.info("Decoded " + Base64.getDecoder().decode(userIdToken));

        response.sendRedirect("/");
    }

}
