package dk.dma.msinm.common.model;

/**
 * Interface to be implemented by the descriptive entities of localizable entities
 */
public interface ILocalizedDesc<E extends ILocalizable> {

    String getLang();

    void setLang(String lang);

    /**
     * Copies the description values from the desc entity to this entity
     * @param desc the description entity to copy from
     */
    void copyDesc(ILocalizedDesc desc);

}
