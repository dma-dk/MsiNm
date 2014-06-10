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
    public synchronized Configuration getMailTemplateConfiguration() {
        if (cfg == null) {
            cfg = new Configuration();
            //cfg.setServletContextForTemplateLoading(context, MAIL_TEMPLATE_FOLDER);
            cfg.setTemplateUpdateDelay(0);
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        }
        return cfg;
    }

}
