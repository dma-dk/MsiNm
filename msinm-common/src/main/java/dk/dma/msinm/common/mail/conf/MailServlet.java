package dk.dma.msinm.common.mail.conf;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.slf4j.Logger;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet called to generate HTML mails.
 * As a security precaution, this servlet can only be accessed via port 8080
 *
 * <p>
 * The reason it is kept in it's own package is so that the entire package
 * can be CDI @Vetoed from the test project.
 * </p>
 */
@WebServlet(value = "/mail/*", loadOnStartup = 1)
public class MailServlet  extends HttpServlet {

    private static final String MAIL_TEMPLATE_FOLDER = "/WEB-INF/mail/";

    private static Configuration cfg;

    @Inject
    Logger log;

    @Inject
    ServletContext context;

    @Produces
    public synchronized Configuration getMailTemplateConfiguration() {
        if (cfg == null) {
            cfg = new Configuration();
            cfg.setServletContextForTemplateLoading(context, MAIL_TEMPLATE_FOLDER);
            cfg.setTemplateUpdateDelay(0);
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        }
        return cfg;
    }

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

        String template = request.getPathInfo();
        if (template.endsWith(".ftl")) {
            try {
                log.info("Processing mail freemarker template: " + template);

                Template fmTemplate = getMailTemplateConfiguration().getTemplate(template);
                Map<String, Object> data = new HashMap<>();

                // Add request parameters as data
                request.getParameterMap().forEach((name, values) -> {
                    if (values == null || values.length == 0) {
                        data.put(name, "");
                    } else if (values.length == 1) {
                        data.put(name, values[0]);
                    } else {
                        data.put(name, Arrays.asList(values));
                    }
                });

                fmTemplate.process(data, response.getWriter());

            } catch (Exception e) {
                log.error("Error processing mail freemarker template: " + template, e);
            }
        } else {
            log.warn("Invalid template " + template);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }


    }

}
