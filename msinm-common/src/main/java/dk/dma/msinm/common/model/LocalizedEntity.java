package dk.dma.msinm.common.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for localized entities
 */
@MappedSuperclass
public abstract class LocalizedEntity<D extends DescEntity> extends VersionedEntity<Integer> {

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "entity")
    List<D> descs = new ArrayList<>();

    public List<D> getDescs() {
        return descs;
    }

    public void setDescs(List<D> descs) {
        this.descs = descs;
    }

    /**
     * Returns the localized description for the given language.
     * Returns null if the description is not defined.
     *
     * @param lang the language
     * @return the localized description for the given language
     */
    @Transient
    public D getDesc(String lang) {
        for (D desc : getDescs()) {
            if (desc.getLang().equalsIgnoreCase(lang)) {
                return desc;
            }
        }
        return null;
    }

    /**
     * Initializes the description entity by setting the language
     * and adding it to the list of description entities.
     *
     * If the list of description entities was empty, the desc entity
     * is also set as the main description entity
     *
     * @param desc the description entity to initialize
     * @param lang the language
     * @return the initialized description entity
     */
    @SuppressWarnings("unchecked")
    protected D initDesc(D desc, String lang) {
        desc.setLang(lang);
        desc.setEntity(this);
        descs.add(desc);
        return desc;
    }

    /**
     * Creates the localized description for the given language
     * and adds it to the list of description entities.
     *
     * @param lang the language
     * @return the created description
     */
    protected abstract D createDesc(String lang);


    /**
     * Returns the localized description for the given language.
     * Creates a new description entity if none exists in advance.
     *
     * @param lang the language
     * @return the localized description for the given language
     */
    @Transient
    public D getOrCreateDesc(String lang) {
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
    public void copyDescs(List<D> descs) {
        for (D desc : descs) {
            getOrCreateDesc(desc.getLang()).copyDesc(desc);
        }
    }
}
