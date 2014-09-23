package dk.dma.msinm.common.templates;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

/**
 * A singleton wrapper of a Freemarker configuration
 */
@Singleton
public class TemplateConfiguration {

    Configuration cfg;

    /**
     * Initializes the Freemarker configuration
     */
    @PostConstruct
    public void init() {
        cfg = new Configuration();
        cfg.setLocalizedLookup(true);
        cfg.setClassForTemplateLoading(getClass(), TemplateContext.TEMPLATE_ROOT);
        cfg.setTemplateUpdateDelay(0);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
    }

    /**
     * Returns a reference to the Freemarker configuration
     * @return a reference to the Freemarker configuration
     */
    public Configuration getConfiguration() {
        return cfg;
    }
}
