package dk.dma.msinm.common.templates;

import dk.dma.msinm.common.MsiNmApp;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.ResourceBundleModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Main interface for accessing and producing Freemarker templates,
 * and for processing the templates
 */
public class TemplateService {

    private static final String BUNDLE_PROPERTY = "text";

    @Inject
    private Logger log;

    @Inject
    MsiNmApp app;

    @Inject
    Configuration templateConfiguration;

    /**
     * Produces a new {@code TemplateContext} instantiated with the Freemarker configuration
     * @return a new {@code TemplateContext}
     */
    @Produces
    public TemplateContext getTemplateContext() {
        return new TemplateContext(templateConfiguration);
    }

    /**
     * Creates and instantiates a new {@code TemplateContext} with the given parameters
     * @param type the template type
     * @param templatePath the template path
     * @param data the data
     * @return the {@code TemplateContext}
     */
    public TemplateContext getTemplateContext(TemplateType type, String templatePath, Map<String, Object> data) {
        TemplateContext ctx = getTemplateContext();

        ctx.setType(type)
                .setTemplatePath(templatePath)
                .setData(data);

        // Standard data properties
        ctx.getData().put("baseUri", app.getBaseUri());
        ctx.getData().put("organization", app.getOrganization());
        // TODO: more standard properties (org name, email, etc)

        return ctx;
    }

    /**
     * Creates and instantiates a new {@code TemplateContext} with the given parameters
     * @param type the template type
     * @param templatePath the template path
     * @param data the data
     * @param language the language
     * @param bundleName the resource bundle to load
     * @return the {@code TemplateContext}
     */
    public TemplateContext getTemplateContext(TemplateType type, String templatePath, Map<String, Object> data, String language, String bundleName) {
        TemplateContext ctx = getTemplateContext(type, templatePath, data);

        // Load the resource bundle with the given language and name, and save it in the "text" data property
        Locale locale = app.getLocale(language);
        ResourceBundle bundle = null;
        try {
            bundle = ResourceBundle.getBundle(ctx.getFullBundleName(bundleName), locale);
        } catch (Exception e) {
            // Fall back to using the english locale
            locale = Locale.ENGLISH;
            bundle = ResourceBundle.getBundle(ctx.getFullBundleName(bundleName), locale);
        }
        ResourceBundleModel resourceBundleModel = new ResourceBundleModel(bundle, new BeansWrapper());

        ctx.setLocale(locale);
        ctx.setBundle(bundle);
        ctx.getData().put(BUNDLE_PROPERTY, resourceBundleModel);

        return ctx;
    }

    /**
     * Process the Freemarker template defined by the given context
     * @param templateContext the template context
     * @return the generated html
     */
    public String process(TemplateContext templateContext) throws IOException, TemplateException {
        try {
            Template fmTemplate = templateContext.getFreemarkerTemplate();

            StringWriter html = new StringWriter();
            fmTemplate.process(templateContext.getData(), html);

            return html.toString();
        } catch (IOException e) {
            log.error("Failed loading freemarker template " + templateContext.getFullTemplatePath(), e);
            throw e;
        } catch (TemplateException e) {
            log.error("Failed executing freemarker template " + templateContext.getFullTemplatePath(), e);
            throw e;
        }
    }

}
