package dk.dma.msinm.templates.model;

import dk.dma.msinm.common.model.DescEntity;
import dk.dma.msinm.common.model.ILocalizedDesc;

import javax.persistence.Entity;

/**
 * Localized contents for the {@code ListParamValue} entity
 */
@Entity
public class ListParamValueDesc extends DescEntity<ListParamValue> {

    private String shortValue;
    private String longValue;

    /**
     * {@inheritDoc}
     */
    @Override
    public void copyDesc(ILocalizedDesc desc) {
        if (!(desc instanceof ListParamValueDesc)) {
            throw new IllegalArgumentException("Invalid desc class " + desc);
        }
        this.shortValue = ((ListParamValueDesc)desc).getShortValue();
        this.longValue = ((ListParamValueDesc)desc).getLongValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean descDefined() {
        return ILocalizedDesc.fieldsDefined(shortValue, longValue);
    }

    // ***********************************
    // Getters and setters
    // ***********************************

    public String getShortValue() {
        return shortValue;
    }

    public void setShortValue(String shortValue) {
        this.shortValue = shortValue;
    }

    public String getLongValue() {
        return longValue;
    }

    public void setLongValue(String longValue) {
        this.longValue = longValue;
    }

}

