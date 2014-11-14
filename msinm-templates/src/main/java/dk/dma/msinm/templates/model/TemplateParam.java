package dk.dma.msinm.templates.model;

import dk.dma.msinm.common.model.BaseEntity;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

/**
 * Entity class for the message template parameters
 */
@Entity
public class TemplateParam extends BaseEntity<Integer> {

    /**
     * The type is a "loose" reference to a named parameter type.
     * The parameter type may be one of:
     * <ul>
     *     <li>A base parameter type, either "text", "number", "boolean" or "date".</li>
     *     <li>A list parameter type as defined by the {@code ListParamType} entity.</li>
     *     <li>A composite parameter type as defined by the {@code CompositeParamType} entity.</li>
     * </ul>
     */
    @NotNull
    String type;

    @NotNull
    String name;

    boolean mandatory;

    boolean list;

    int sortKey;

    // ***********************************
    // Getters and setters
    // ***********************************

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean isList() {
        return list;
    }

    public void setList(boolean list) {
        this.list = list;
    }

    public int getSortKey() {
        return sortKey;
    }

    public void setSortKey(int sortKey) {
        this.sortKey = sortKey;
    }
}
