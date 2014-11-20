package dk.dma.msinm.templates.vo;

import dk.dma.msinm.common.vo.BaseVo;
import dk.dma.msinm.templates.model.FieldTemplate;
import dk.dma.msinm.templates.model.Template;
import org.apache.commons.lang.StringUtils;

/**
 * Value object for the {@code FieldTemplate} model entity
 */
public class FieldTemplateVo extends BaseVo<FieldTemplate> {

    String field;
    String lang;
    String fmTemplate;
    int sortKey;
    boolean defaultField;
    String result;
    String error;

    /**
     * Constructor
     */
    public FieldTemplateVo() {
    }

    /**
     * Constructor
     */
    public FieldTemplateVo(String field, String lang, int sortKey, boolean defaultField) {
        this.field = field;
        this.lang = lang;
        this.sortKey = sortKey;
        this.defaultField = defaultField;
    }

    /**
     * Constructor
     *
     * @param fieldTemplate the entity
     */
    public FieldTemplateVo(FieldTemplate fieldTemplate) {
        super(fieldTemplate);

        field = fieldTemplate.getField();
        lang = fieldTemplate.getLang();
        fmTemplate = fieldTemplate.getFmTemplate();
        sortKey = fieldTemplate.getSortKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FieldTemplate toEntity() {
        FieldTemplate fieldTemplate = new FieldTemplate();
        fieldTemplate.setField(field);
        fieldTemplate.setLang(lang);
        fieldTemplate.setFmTemplate(fmTemplate);
        fieldTemplate.setSortKey(sortKey);
        return fieldTemplate;
    }

    /**
     * Converts the VO to an entity associated with the given template
     * @param template the parent template
     */
    public FieldTemplate toEntity(Template template) {
        FieldTemplate fieldTemplate = toEntity();
        fieldTemplate.setTemplate(template);
        return fieldTemplate;
    }

    /**
     * Returns if this field template defines a Freemarker template
     * @return if this field template defines a Freemarker template
     */
    public boolean isDefined() {
        return StringUtils.isNotBlank(fmTemplate);
    }

    /**
     * Returns the result, or if defined, the error
     * @return the result, or if defined, the error
     */
    public String errorOrResult() {
        return StringUtils.isNotBlank(error) ? error : result;
    }

    // ***********************************
    // Getters and setters
    // ***********************************

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getFmTemplate() {
        return fmTemplate;
    }

    public void setFmTemplate(String fmTemplate) {
        this.fmTemplate = fmTemplate;
    }

    public int getSortKey() {
        return sortKey;
    }

    public void setSortKey(int sortKey) {
        this.sortKey = sortKey;
    }

    public boolean isDefaultField() {
        return defaultField;
    }

    public void setDefaultField(boolean defaultField) {
        this.defaultField = defaultField;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
