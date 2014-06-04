package dk.dma.msinm.common.mail;

import org.slf4j.Logger;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet called to generate HTML mails.
 * As a security precaution, this servlet can only be accessed via port 8080
 */
@WebServlet(value = "/mail/*")
public class MailServlet  extends HttpServlet {

    private static final String MAIL_JSP_FOLDER = "/WEB-INF/jsp/mail/";

    @Inject
    Logger log;

    /**
     * Main GET method
     *
     * @param request servlet request
     * @param response servlet response
     * @throws java.io.IOException
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // We only allow access via port 8080
        if (request.getServerPort() != 8080) {
            log.warn("Illegal access to mail servlet on port " + request.getServerPort());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String jsp = request.getPathInfo();
        if (!jsp.endsWith(".jsp")) {
            log.warn("Invalid jsp " + jsp);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            log.info("Forwarding to mail jsp: " + jsp);
            getServletConfig().getServletContext()
                    .getRequestDispatcher(MAIL_JSP_FOLDER + jsp)
                    .forward(request, response);

        } catch (ServletException e) {
            log.error("Error forwarding to mail jsp: " + jsp, e);
        }

    }

}
