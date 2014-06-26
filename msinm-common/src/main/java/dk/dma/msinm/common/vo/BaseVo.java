package dk.dma.msinm.common.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dk.dma.msinm.common.model.BaseEntity;

import java.io.Serializable;

/**
 * Base class for value objects.
 * It essentially defines two ways of using the VO:
 * <ul>
 *     <li>A constructor that takes a BaseEntity is used for converting an entity into a VO.</li>
 *     <li>A {@code toEntity()} method is used for converting the VO back into an entity.</li>
 * </ul>
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
