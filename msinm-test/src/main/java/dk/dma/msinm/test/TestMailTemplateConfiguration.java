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
package dk.dma.msinm.test;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

import javax.enterprise.inject.Produces;

/**
 * Returns the Freemarker mail template configuration.
 */
public class TestMailTemplateConfiguration {

    private static Configuration cfg;

    @Produces
    public synchronized Configuration getTemplateConfiguration() {
        if (cfg == null) {
            cfg = new Configuration();
            //cfg.setServletContextForTemplateLoading(context, MAIL_TEMPLATE_FOLDER);
            cfg.setTemplateUpdateDelay(0);
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        }
        return cfg;
    }

}
