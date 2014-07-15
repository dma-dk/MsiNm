package dk.dma.msinm.common.templates;

import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.IOException;
import java.util.*;

/**
 * Defines a Freemarker template context
 */
public class TemplateContext {

    public static final String TEMPLATE_ROOT    = "/WEB-INF/classes/templates";
    public static final String BUNDLE_ROOT      = "templates";
    public static final String ENCODING         = "UTF-8";

    Configuration configuration;
    TemplateType type;
    String templatePath;
    Map<String, Object> data = new HashMap<>();
    Locale locale;
    ResourceBundle bundle;

    /**
     * Constructor
     * @param configuration the Freemarker configuration
     */
    protected TemplateContext(Configuration configuration) {
        Objects.requireNonNull(configuration);
        this.configuration = configuration;
    }

    /**
     * Returns the full template path based on the type and template path
     * @return the full template
     */
    public String getFullTemplatePath() {
        return type == null ? templatePath : type.getPath() + "/" + templatePath;
    }

    /**
     * Returns the full resource bundle name based on the type and bundle name
     * @param bundle the local bundle name
     * @return the full resource bundle name
     */
    public String getFullBundleName(String bundle) {
        return type == null
                ? BUNDLE_ROOT + "." + bundle
                : BUNDLE_ROOT + "." + type.getPath() + "." + bundle;
    }

    /**
     * Returns the Freemarker template designated by this context
     * @return the Freemarker template designated by this context
     */
    public Template getFreemarkerTemplate() throws IOException {
        return locale == null
                ? configuration.getTemplate(getFullTemplatePath(), ENCODING)
                : configuration.getTemplate(getFullTemplatePath(), locale, ENCODING);
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public TemplateType getType() {
        return type;
    }

    public TemplateContext setType(TemplateType type) {
        this.type = type;
        return this;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public TemplateContext setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public TemplateContext setData(Map<String, Object> data) {
        this.data = data;
        return this;
    }

    public Locale getLocale() {
        return locale;
    }

    public TemplateContext setLocale(Locale locale) {
        this.locale = locale;
        return this;
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    public TemplateContext setBundle(ResourceBundle bundle) {
        this.bundle = bundle;
        return this;
    }
}
