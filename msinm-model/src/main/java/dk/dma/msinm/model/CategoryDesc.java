package dk.dma.msinm.model;

import dk.dma.msinm.common.model.DescEntity;
import dk.dma.msinm.common.model.ILocalizedDesc;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

/**
 * Localized contents for the Category entity
 */
@Entity
public class CategoryDesc extends DescEntity<Category> {

    @NotNull
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copyDesc(ILocalizedDesc desc) {
        if (!(desc instanceof CategoryDesc)) {
            throw new IllegalArgumentException("Invalid desc class " + desc);
        }
        this.name = ((CategoryDesc)desc).getName();
    }
}
