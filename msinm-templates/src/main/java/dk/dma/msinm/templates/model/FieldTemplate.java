package dk.dma.msinm.templates.model;

import dk.dma.msinm.common.model.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

/**
 * Entity class for the Freemarker field templates.
 */
@Entity
public class FieldTemplate extends BaseEntity<Integer> {

    @ManyToOne
    @NotNull
    Template template;

    @NotNull
    String field;

    @NotNull
    String lang;

    @Lob
    String fmTemplate;

    int sortKey;

    // ***********************************
    // Getters and setters
    // ***********************************

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

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
}
