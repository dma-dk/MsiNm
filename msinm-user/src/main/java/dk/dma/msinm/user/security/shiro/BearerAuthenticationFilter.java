package dk.dma.msinm.user.security.shiro;

import dk.dma.msinm.common.config.CdiHelper;
import dk.dma.msinm.user.security.JWTService;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Shiro authentication filter that handles JWT bearer tokens
 */
public class BearerAuthenticationFilter extends AuthenticatingFilter {

    private final static String BEARER_TOKEN_HEADER = "Authorization";
    private final static String BEARER_TOKEN_PREFIX = "Bearer ";

    /**
     * Returns the bearer token or null if not present
     *
     * @param request the servlet request
     * @return the bearer token or null if not present
     */
    protected String getBearerToken(ServletRequest request) {

        System.out.println("XXXXXX GET BEARER TOKEN");

        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        String header = httpRequest.getHeader(BEARER_TOKEN_HEADER);
        if (header != null && header.startsWith(BEARER_TOKEN_PREFIX)) {
            return header.substring(BEARER_TOKEN_PREFIX.length()).trim();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        if (getBearerToken(request) != null) {
            return executeLogin(request, response);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final boolean isLoginRequest(ServletRequest request, ServletResponse response) {
        return getBearerToken(request) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
        // Determine the site
        try {
            JWTService jwtService = CdiHelper.getBean(JWTService.class);

            // Create the authentication token
            String token = getBearerToken(request);
            if (token != null) {
                // Bearer token authentication
                String username = jwtService.extractBearerTokenUser(token);
                return new BearerToken(username, token, getHost(request));
            } else {
                // Basic authentication
                return createToken("", "", request, response);
            }

        } catch (Exception ex) {
            return null;
        }

    }

}
