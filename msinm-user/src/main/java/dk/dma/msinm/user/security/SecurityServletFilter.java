package dk.dma.msinm.user.security;


import dk.dma.msinm.common.audit.Auditor;
import dk.dma.msinm.common.web.WebUtils;
import dk.dma.msinm.user.User;
import dk.dma.msinm.user.UserService;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles security in the application
 */
@WebFilter(urlPatterns={"*"})
public class SecurityServletFilter implements Filter {

    private final static String BEARER_TOKEN_HEADER = "Authorization";
    private final static String BEARER_TOKEN_PREFIX = "Bearer ";

    @Inject
    private Logger log;

    @Inject
    private JWTService jwtService;

    @Inject
    private UserService userService;

    @Inject
    Auditor auditor;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
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

        // Check if the request is handled as a user-pwd login attempt
        if (handleUserPasswordAuth(request, response)) {
            return;
        }

        // If the request contains a JWT token header, attempt a login on the request
        attemptJwtLogin(request);

        // Propagate the request
        chain.doFilter(request, response);
    }

    /**
     * If the request contains a JWT header, the user will be logged in for this request using the token.
     * <p>
     * If the authentication fails, this methods does nothing. It is left to the handler of the request,
     * say a Rest endpoint, to throw an error if security requirements are not met.
     *
     * @param request the servlet request
     */
    public void attemptJwtLogin(HttpServletRequest request)  {
        try {
            String jwt = getJwtBearerToken(request);
            if (jwt != null) {
                request = SecurityUtils.login(userService, request, jwt, JbossLoginModule.BEARER_TOKEN_LOGIN);
                log.trace("Found JWT user " + request.getUserPrincipal().getName());
            }
        } catch (ServletException ex) {
            log.warn("Failed logging in using bearer token");
            //ex.printStackTrace();
        }
    }

    /**
     * Checks if the request is an attempt to perform user-password authentication
     *
     * @param request the servlet request
     * @param response the servlet response
     * @return if this request has been handled
     */
    public boolean handleUserPasswordAuth(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!WebUtils.getServletUrl(request).endsWith("/auth")) {
            return false;
        }

        Credentials credentials = Credentials.fromRequest(request);
        if (credentials != null) {
            log.info("Logging in with email " + credentials.getEmail());

            // Successful login - create a JWT token
            String svr = String.format("%s://%s", request.getScheme(), request.getServerName());
            try {
                request = SecurityUtils.login(userService, request, credentials.getEmail(), credentials.getPassword());

                JWTToken jwt = jwtService.getSignedJWT(svr, (User)request.getUserPrincipal());
                response.setContentType("application/json");
                response.setHeader("Cache-Control", "no-cache") ;
                response.setHeader("Expires", "0") ;
                response.getWriter().write(jwt.toJson());
                auditor.info("User %s logged in. Issued token %s", credentials.getEmail(), jwt.getToken());
                return true;
            } catch (Exception ex) {
            }
        }

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        log.warn("Failed logging in using email and password");
        return true;
    }

    /**
     * Returns the bearer token or null if not present
     *
     * @param request the servlet request
     * @return the bearer token or null if not present
     */
    protected String getJwtBearerToken(HttpServletRequest request) {

        String header = request.getHeader(BEARER_TOKEN_HEADER);
        if (header != null && header.startsWith(BEARER_TOKEN_PREFIX)) {
            return header.substring(BEARER_TOKEN_PREFIX.length()).trim();
        }
        return null;
    }
}

