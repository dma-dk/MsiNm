package dk.dma.msinm.user.security;


import dk.dma.msinm.common.audit.Auditor;
import dk.dma.msinm.common.settings.annotation.Setting;
import dk.dma.msinm.user.User;
import dk.dma.msinm.user.UserService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.Date;

import static dk.dma.msinm.user.security.SecurityConf.CheckedResource;

/**
 * Handles security in the application
 */
@WebFilter(urlPatterns={"*"})
public class SecurityServletFilter implements Filter {

    private final static String AUTHORIZATION_HEADER = "Authorization";
    private final static String JWT_TOKEN = "Bearer ";
    private final static String BASIC_AUTH = "Basic ";
    public final static String AUTH_ERROR_ATTR = "msinm.auth.error";

    private SecurityConf securityConf;

    @Inject
    @Setting(value = "securityConfFile", defaultValue = "/WEB-INF/msinm-security.ini")
    String securityConfFile;

    @Inject
    private Logger log;

    @Inject
    private JWTService jwtService;

    @Inject
    private UserService userService;

    @Inject
    Auditor auditor;

    /**
     * Initializes the security configuration
     * @param filterConfig the servlet filter configuration
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialize the security configuration
        if (StringUtils.isNotBlank(securityConfFile)) {
            try {
                securityConf = new SecurityConf(filterConfig.getServletContext().getResourceAsStream(securityConfFile));
            } catch (Exception e) {
                log.error("Error loading security config file " + securityConfFile, e);
                throw new ServletException("Error loading security config file " + securityConfFile, e);
            }
        } else {
            securityConf = new SecurityConf();
        }
        log.info("Loaded security configuration " + securityConf);
    }

    @Override
    public void destroy() {
    }

    /**
     * Main filter method
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // As of Wildfly 8.1, once a request has executed a successful request.login(),
        // subsequent requests will stay logged in. Surely this is an error(?).
        // Hence, we ensure that the request is logged out by default:
        try {
            request.logout();
        } catch (ServletException e) {
            log.debug("Failed logging request out");
        }

        // Check if the security filter needs to process this requested resource
        if (securityConf.checkResource(request)) {

            // Handle JWT
            if (securityConf.supportsJwtAuth()) {

                // Check if the request is a user-pwd login attempt
                if (securityConf.isJwtAuthEndpoint(request)) {
                    handleJwtUserPasswordAuth(request, response);
                    return;
                }

                // If the request contains a JWT token header, attempt a login on the request
                request = attemptJwtAuthLogin(request, response);
            }

            // Handle Basic Authentication
            if (securityConf.supportsBasicAuth()) {
                // If the request contains a Basic authentication header, attempt a login
                request = attemptBasicAuthLogin(request);
            }

            // Check if the request resource specifies a required role
            CheckedResource errorResource = securityConf.lacksRequiredRole(request);
            if (errorResource != null) {
                log.error("User must have role " + errorResource.getRequiredRoles() + " for resource " + errorResource.getUri());
                if (StringUtils.isNotBlank(errorResource.getRedirect())) {
                    response.sendRedirect(errorResource.getRedirect());
                } else {
                    response.setStatus(getErrorStatusCode(request, HttpServletResponse.SC_UNAUTHORIZED));
                }
                return;
            }
        }

        // Propagate the request
        chain.doFilter(request, response);
    }

    /**
     * If the request contains a Basic authentication header, the user will be logged in for this request
     * using the specified credentials.
     * <p>
     * If the authentication fails, this methods does nothing. It is left to the handler of the request,
     * say a Rest endpoint, to throw an error if security requirements are not met.
     *
     * @param request the servlet request
     * @return the request
     */
    private HttpServletRequest attemptBasicAuthLogin(HttpServletRequest request) {
        try {
            String token = getAuthHeaderToken(request, BASIC_AUTH);
            if (token != null) {
                String[] cred = new String(Base64.getDecoder().decode(token), "UTF-8").split(":");
                request = SecurityUtils.login(userService, request, cred[0], cred[1]);
                log.trace("Found Basic Auth user " + request.getUserPrincipal().getName());
            }
        } catch (Exception ex) {
            request.setAttribute(AUTH_ERROR_ATTR, HttpServletResponse.SC_UNAUTHORIZED);
            log.warn("Failed logging in using Basic Authentication");
        }
        return request;
    }

    /**
     * If the request contains a JWT header, the user will be logged in for this request using the token.
     * <p>
     * If the authentication fails, this methods does nothing. It is left to the handler of the request,
     * say a Rest endpoint, to throw an error if security requirements are not met.
     *
     * @param request the servlet request
     * @return the request
     */
    public HttpServletRequest attemptJwtAuthLogin(HttpServletRequest request, HttpServletResponse response)  {
        try {
            // Get the JWT token from the header
            String jwt = getAuthHeaderToken(request, JWT_TOKEN);

            if (jwt != null) {
                // Parse and verify the JWT token
                JWTService.ParsedJWTInfo jwtInfo = jwtService.parseSignedJWT(jwt);

                // Check if the bearer token has expired
                Date now = new Date();
                if (now.after(jwtInfo.getExpirationTime())) {
                    request.setAttribute(AUTH_ERROR_ATTR, 419); // 419: session timed out
                    log.warn("JWT token expired for user " + jwtInfo.getSubject());
                    return request;
                }

                // Before logging in, generate a one-time password token tied to the current thread.
                // This is verified in the JbossLoginModule
                String tempPwd = jwtService.generateTempJwtPwdToken(JbossLoginModule.BEARER_TOKEN_LOGIN);
                request = SecurityUtils.login(userService, request, jwtInfo.getSubject(), tempPwd);
                log.trace("Found JWT user " + request.getUserPrincipal().getName());

                // After a configurable amount of minutes, a new JWT token will automatically be
                // issued and sent back to the client.
                // This will allow the client to implement inactivity timeout instead of relying on
                // the fixed expiration date of the JWT token.
                if (jwtService.reauthJWT(jwtInfo)) {
                    log.info("New JWT token issued for re-authorization of user " + jwtInfo.getSubject());
                    JWTToken reAuthJwt = jwtService.createSignedJWT(getJwtIssuer(request), (User) request.getUserPrincipal());
                    response.setHeader("Reauthorization", reAuthJwt.getToken());
                }
            }
        } catch (Exception ex) {
            request.setAttribute(AUTH_ERROR_ATTR, HttpServletResponse.SC_UNAUTHORIZED);
            log.warn("Failed logging in using bearer token");
        }
        return request;
    }

    /**
     * Checks if the request is an attempt to perform user-password authentication
     *
     * @param request the servlet request
     * @param response the servlet response
     * @return the request
     */
    public HttpServletRequest handleJwtUserPasswordAuth(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Credentials credentials = Credentials.fromRequest(request);
        if (credentials != null) {
            log.info("Logging in with email " + credentials.getEmail());

            // Successful login - create a JWT token
            try {
                request = SecurityUtils.login(userService, request, credentials.getEmail(), credentials.getPassword());

                JWTToken jwt = jwtService.createSignedJWT(getJwtIssuer(request), (User) request.getUserPrincipal());
                response.setContentType("application/json");
                response.setHeader("Cache-Control", "no-cache") ;
                response.setHeader("Expires", "0") ;
                response.getWriter().write(jwt.toJson());
                auditor.info("User %s logged in. Issued token %s", credentials.getEmail(), jwt.getToken());
                return request;
            } catch (Exception ex) {
            }
        }

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        log.warn("Failed logging in using email and password");
        return request;
    }

    /**
     * Returns the JWT issuer based on the current server name
     * @param request the servlet request
     * @return the JWT issuer
     */
    protected String getJwtIssuer(HttpServletRequest request) {
        return String.format("%s://%s", request.getScheme(), request.getServerName());
    }

    /**
     * Returns the Authorization header token or null if not present
     *
     * @param request the servlet request
     * @return the Authorization header token or null if not present
     */
    protected String getAuthHeaderToken(HttpServletRequest request, String autType) {

        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header != null && header.startsWith(autType)) {
            return header.substring(autType.length()).trim();
        }
        return null;
    }

    /**
     * Returns the response status code to use for this request.
     * If the AUTH_ERROR_ATTR request attribute has been set then this is returned,
     * otherwise, the {@code defaultStatusCode is returned.
     *
     * @param defaultStatusCode the default status code to return
     * @param request the request
     * @return the status code to use
     */
    public static int getErrorStatusCode(HttpServletRequest request, int defaultStatusCode) {
        if (request.getAttribute(SecurityServletFilter.AUTH_ERROR_ATTR) != null) {
            return (Integer) request.getAttribute(SecurityServletFilter.AUTH_ERROR_ATTR);
        }
        return defaultStatusCode;
    }
}

