package dk.dma.msinm.templates.model;

import dk.dma.msinm.common.model.DescEntity;
import dk.dma.msinm.common.model.ILocalizedDesc;

import javax.persistence.Entity;

/**
 * Localized contents for the {@code DictTerm} entity
 */
@Entity
public class DictTermDesc extends DescEntity<DictTerm> {

    private String value;

    /**
     * {@inheritDoc}
     */
    @Override
    public void copyDesc(ILocalizedDesc desc) {
        if (!(desc instanceof DictTermDesc)) {
            throw new IllegalArgumentException("Invalid desc class " + desc);
        }
        this.value = ((DictTermDesc)desc).getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean descDefined() {
        return ILocalizedDesc.fieldsDefined(value);
    }

    // ***********************************
    // Getters and setters
    // ***********************************


    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
