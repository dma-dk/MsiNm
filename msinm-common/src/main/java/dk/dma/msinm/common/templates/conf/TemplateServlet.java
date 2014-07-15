/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.msinm.common.templates.conf;

import dk.dma.msinm.common.templates.TemplateContext;
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
 * Servlet called to generate Freemarker templates.
 * As a security precaution, this servlet can only be accessed via port 8080
 *
 * <p>
 * The reason it is kept in it's own package is so that the entire package
 * can be CDI @Vetoed from the test project.
 * </p>
 */
@WebServlet(value = "/template/*", loadOnStartup = 1)
public class TemplateServlet extends HttpServlet {

    private static Configuration cfg;

    @Inject
    Logger log;

    @Inject
    ServletContext context;

    @Produces
    public synchronized Configuration getTemplateConfiguration() {
        if (cfg == null) {
            cfg = new Configuration();
            cfg.setLocalizedLookup(true);
            cfg.setServletContextForTemplateLoading(context, TemplateContext.TEMPLATE_ROOT);
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
            log.warn("Illegal access to template servlet on port " + request.getServerPort());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String template = request.getPathInfo();
        if (template.endsWith(".ftl")) {
            try {
                log.info("Processing freemarker template: " + template);

                Template fmTemplate = getTemplateConfiguration().getTemplate(template);
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
                log.error("Error processing freemarker template: " + template, e);
            }
        } else {
            log.warn("Invalid template " + template);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }


    }

}
