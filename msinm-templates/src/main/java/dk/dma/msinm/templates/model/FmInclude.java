package dk.dma.msinm.templates.model;

import dk.dma.msinm.common.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;

/**
 * Entity class for Freemarker templates that gets included in all field templates.
 */
@Entity
@NamedQueries({
        @NamedQuery(name  = "FmInclude.findAll",
                query = "select fm from FmInclude fm order by lower(fm.name) asc")
})
public class FmInclude extends BaseEntity<Integer> {

    @NotNull
    @Column(unique = true)
    String name;

    @Lob
    String fmTemplate;

    // ***********************************
    // Getters and setters
    // ***********************************

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFmTemplate() {
        return fmTemplate;
    }

    public void setFmTemplate(String fmTemplate) {
        this.fmTemplate = fmTemplate;
    }
}
