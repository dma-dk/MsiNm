package dk.dma.msinm.user.security;


import dk.dma.msinm.user.UserService;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
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

        try {
            String jwt = getBearerToken(request);
            if (jwt != null) {
                request = SecurityUtils.login(userService, request, jwt, JbossLoginModule.BEARER_TOKEN_LOGIN);
                log.info("Found JWT user " + request.getUserPrincipal().getName());
            }
        } catch (ServletException ex) {
            log.warn("Failed logging in using bearer token");
            //ex.printStackTrace();
        }

        // Propagate the request
        chain.doFilter(request, response);
    }

    /**
     * Returns the bearer token or null if not present
     *
     * @param request the servlet request
     * @return the bearer token or null if not present
     */
    protected String getBearerToken(ServletRequest request) {

        HttpServletRequest httpRequest = (HttpServletRequest)request;
        String header = httpRequest.getHeader(BEARER_TOKEN_HEADER);
        if (header != null && header.startsWith(BEARER_TOKEN_PREFIX)) {
            return header.substring(BEARER_TOKEN_PREFIX.length()).trim();
        }
        return null;
    }


}
