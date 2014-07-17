package dk.dma.msinm.web;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

/**
 * When running HTTPS behind, say, an Apache web server which handles the SSL decoding,
 * Response.sendRedirect() will not work properly when redirecting to a relative path.
 * Since the Servlet container receives a HTTP request, the redirect URL will be
 * using HTTP as well.
 * <p>
 *     If, however, the port-443 VirtualHost is configured to set the header originalScheme=https,
 *     then this filter will ensure that https is used in relative redirects.
 * </p>
 * <p>Example configuration:</p>
 * <pre>
 *     Header add originalScheme "https"
 *     RequestHeader set originalScheme "https"
 * </pre>
 */
@WebFilter(urlPatterns={"/*"})
public class SendRedirectFilter  implements Filter {

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
    }


    /**
     * Main filter method
     * @param req the request
     * @param res the response
     * @param chain the filter chain
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        chain.doFilter(request, new SendRedirectResponse(request, response));
    }

}

/**
 * Overrides sendRedict() to make relative locations absolute and adopt the original scheme
 */
class SendRedirectResponse extends HttpServletResponseWrapper {

    public static final String HEADER_ORIGINAL_SCHEME = "originalScheme";

    private HttpServletRequest request;
    private String prefix = null;

    /**
     * Constructor
     * @param request the request
     * @param response the response
     */
    public SendRedirectResponse(HttpServletRequest request, HttpServletResponse response) {
        super(response);
        this.request = request;
    }

    /**
     * Overrides sendRedict() to make relative locations absolute and adopt the original scheme
     * @param location the location to redirect to
     */
    @Override
    public void sendRedirect(String location) throws IOException {

        // Only tamper with relative locations
        int offset = request.getRequestURL().indexOf(request.getRequestURI());
        boolean wasHttps = "https".equalsIgnoreCase(request.getHeader("originalScheme"));
        if (!location.toLowerCase().startsWith("http") &&
                wasHttps &&
                offset != -1) {
            // Convert to absolute URL
            location = request.getRequestURL().substring(0, offset) + location;
            // Change the scheme to https
            if (location.toLowerCase().startsWith("http://")) {
                location = "https://" + location.substring("http://".length());
            }
        }

        super.sendRedirect(location);
    }
}