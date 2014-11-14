package dk.dma.msinm.templates.vo;

import dk.dma.msinm.common.vo.JsonSerializable;

/**
 * Abstract value object super class for the {@code ParamType} derived model entity
 */
public abstract class ParamTypeVo implements JsonSerializable {

    /**
     * Defines the kind of parameter type
     */
    enum Kind { BASE, LIST, COMPOSITE }

    Integer id;
    String name;
    Kind kind;

    /**
     * Constructor
     */
    public ParamTypeVo() {
    }

    // ***********************************
    // Getters and setters
    // ***********************************

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }
}
