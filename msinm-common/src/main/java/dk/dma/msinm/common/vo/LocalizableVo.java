package dk.dma.msinm.common.vo;

import dk.dma.msinm.common.model.BaseEntity;
import dk.dma.msinm.common.model.ILocalizable;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for localizable VO's
 */
public abstract class LocalizableVo<E extends BaseEntity & ILocalizable, D extends LocalizedDescVo> extends BaseVo<E> implements ILocalizable<D> {

    List<D> descs = new ArrayList<>();

    /**
     * Constructor
     */
    public LocalizableVo() {
        super();
    }

    /**
     * Constructor
     * @param entity the entity of the VO
     */
    public LocalizableVo(E entity) {
        super(entity);
    }

    @Override
    public List<D> getDescs() {
        return descs;
    }

    @Override
    public void setDescs(List<D> descs) {
        this.descs = descs;
    }

}
