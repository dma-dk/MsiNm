package dk.dma.msinm.user.security.oauth;

import dk.dma.msinm.common.MsiNmApp;
import dk.dma.msinm.common.util.WebUtils;
import dk.dma.msinm.user.User;
import dk.dma.msinm.user.UserService;
import dk.dma.msinm.user.security.AuthCache;
import dk.dma.msinm.user.security.JWTService;
import dk.dma.msinm.user.security.JWTToken;
import org.apache.commons.lang.StringUtils;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthConstants;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringReader;
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

    @Inject
    JWTService jwtService;

    @Inject
    UserService userService;

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
            log.info("Received callback request from " + request.getPathInfo());

            String oauthVerifier = request.getParameter("code");
            Verifier verifier = new Verifier(oauthVerifier);

            OAuthService service = new ServiceBuilder()
                    .provider(GoogleApiProvider.class)
                    .apiKey("959114872597-df018493b0c51j3ls0gbdlmcbp8hg6tp.apps.googleusercontent.com")
                    .apiSecret("UUNo-4Y7RxPLlHjiDlKLbF9B")
                    .scope("openid profile email")
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

            String profile = new String(Base64.getDecoder().decode(userIdToken));
            log.info("Decoded " + profile);

            try (JsonReader jsonReader = Json.createReader(new StringReader(profile))) {
                JsonObject json = jsonReader.readObject();

                String email = json.containsKey("email") ? json.getString("email") : null;
                String firstName = json.containsKey("given_name") ? json.getString("given_name") : null;
                String lastName = json.containsKey("family_name") ? json.getString("family_name") : null;
                if (StringUtils.isBlank(email)) {
                    throw new Exception("No email found in OAuth token");
                }
                log.info("Email  " + email);

                User user = userService.findByEmail(email);
                if (user == null) {
                    user = new User();
                    user.setEmail(email);
                    user.setLanguage("en");
                    user.setFirstName(firstName);
                    user.setLastName(StringUtils.isBlank(lastName) ? email : lastName);
                    user = userService.registerOAuthOnlyUser(user);
                }

                // Create a new JWT token
                JWTToken jwt = jwtService.createSignedJWT(getJwtIssuer(request), user);
                String token = jwtService.createTempJwtPwdToken(AuthCache.AUTH_TOKEN_PREFIX);

                // Cache it for at most 1 minute in the auth cache
                authCache.getCache().put(token, jwt);
                log.info("Setting auth token " + token + " to jwt token " + jwt);

                // Redirect to the auth token landing page, which will use the token to authenticate
                WebUtils.nocache(response).sendRedirect("/index.html#/auth/" + token);
                return;
            }



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
