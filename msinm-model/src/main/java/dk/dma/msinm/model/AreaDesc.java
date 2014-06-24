package dk.dma.msinm.model;

import dk.dma.msinm.common.model.DescEntity;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

/**
 * Localized contents for the Area entity
 */
@Entity
public class AreaDesc extends DescEntity<Area> {

    @NotNull
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Copies the description values from the desc entity to this entity
     * @param desc the description entity to copy from
     */
    @Override
    public void copyDesc(DescEntity desc) {
        if (!(desc instanceof AreaDesc)) {
            throw new IllegalArgumentException("Invalid desc class " + desc);
        }
        this.name = ((AreaDesc)desc).getName();
    }
}
