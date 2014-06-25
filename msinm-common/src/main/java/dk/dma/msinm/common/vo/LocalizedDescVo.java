package dk.dma.msinm.common.vo;

import dk.dma.msinm.common.model.BaseEntity;
import dk.dma.msinm.common.model.ILocalizedDesc;

/**
 * Base class for VO's of localizable entities
 */
public abstract class LocalizedDescVo <E extends BaseEntity & ILocalizedDesc, L extends LocalizableVo> extends BaseVo<E> implements ILocalizedDesc<L> {

    String lang;

    /**
     * Constructor
     */
    public LocalizedDescVo() {
        super();
    }

    /**
     * Constructor
     * @param entity the entity of the VO
     */
    public LocalizedDescVo(E entity) {
        super(entity);
        this.lang = entity.getLang();
    }

    @Override
    public String getLang() {
        return lang;
    }

    @Override
    public void setLang(String lang) {
        this.lang = lang;
    }

    @Override
    public void copyDesc(ILocalizedDesc desc) {
        // Not used for VO's
    }
}
