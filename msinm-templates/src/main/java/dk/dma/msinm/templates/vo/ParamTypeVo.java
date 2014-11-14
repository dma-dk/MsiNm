package dk.dma.msinm.templates.vo;

import dk.dma.msinm.common.vo.BaseVo;
import dk.dma.msinm.templates.model.ParamType;

/**
 * Abstract value object super class for the {@code ParamType} derived model entity
 */
public abstract class ParamTypeVo<T extends ParamType> extends BaseVo<T> {
    Integer id;
    String name;

    /**
     * Constructor
     */
    public ParamTypeVo() {
    }

    /**
     * Constructor
     *
     * @param paramType the entity
     */
    public ParamTypeVo(T paramType) {
        super(paramType);

        id = paramType.getId();
        name = paramType.getName();
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
