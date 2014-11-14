package dk.dma.msinm.templates.model;

import dk.dma.msinm.common.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * Base class for the template list parameter type and composite parameter type
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ParamType extends BaseEntity<Integer> {

    @Column(unique=true)
    protected String name;

    // ***********************************
    // Getters and setters
    // ***********************************

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
