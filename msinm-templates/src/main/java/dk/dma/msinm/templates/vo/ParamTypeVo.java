package dk.dma.msinm.templates.vo;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dk.dma.msinm.common.vo.JsonSerializable;

/**
 * Abstract value object super class for the {@code ParamType} derived model entity
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "kind")
@JsonSubTypes({
        @JsonSubTypes.Type(value=BaseParamTypeVo.class, name="BASE"),
        @JsonSubTypes.Type(value=CompositeParamTypeVo.class, name="COMPOSITE"),
        @JsonSubTypes.Type(value=ListParamTypeVo.class, name="LIST")
})
public abstract class ParamTypeVo implements JsonSerializable {

    Integer id;
    String name;

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
}
