package dk.dma.msinm.common.model;

import javax.persistence.*;
import java.util.List;

/**
 * Interface to be implemented by localized entities
 */
public interface ILocalizable<D extends ILocalizedDesc> {

    public List<D> getDescs();

    public void setDescs(List<D> descs);

    /**
     * Returns the localized description for the given language.
     * Returns null if the description is not defined.
     *
     * @param lang the language
     * @return the localized description for the given language
     */
    @Transient
    default public D getDesc(String lang) {
        for (D desc : getDescs()) {
            if (desc.getLang().equalsIgnoreCase(lang)) {
                return desc;
            }
        }
        return null;
    }

    /**
     * Creates the localized description for the given language
     * and adds it to the list of description entities.
     *
     * @param lang the language
     * @return the created description
     */
    public D createDesc(String lang);


    /**
     * Returns the localized description for the given language.
     * Creates a new description entity if none exists in advance.
     *
     * @param lang the language
     * @return the localized description for the given language
     */
    @Transient
    default public D getOrCreateDesc(String lang) {
        D desc = getDesc(lang);
        if (desc == null) {
            desc = createDesc(lang);
        }
        return desc;
    }

    /**
     * Copies the descriptive fields of the list of descriptions
     * @param descs the description entities to copy
     */
    default public void copyDescs(List<D> descs) {
        descs.forEach(desc -> getOrCreateDesc(desc.getLang()).copyDesc(desc));
    }
}
