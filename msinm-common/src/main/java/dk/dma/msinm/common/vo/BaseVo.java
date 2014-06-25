package dk.dma.msinm.common.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dk.dma.msinm.common.model.BaseEntity;

import java.io.Serializable;

/**
 * Base class for value objects
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public abstract class BaseVo<E extends BaseEntity> implements Serializable {

    /**
     * Constructor
     */
    public BaseVo() {
    }

    /**
     * Constructor
     * Use this when the VO is constructed from the entity
     * @param entity the entity of the VO
     */
    public BaseVo(E entity) {
    }

    /**
     * Converts the VO to an entity
     * @return the entity
     */
    public abstract E toEntity();

}
