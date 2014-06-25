package dk.dma.msinm.model;

import dk.dma.msinm.common.model.DescEntity;
import dk.dma.msinm.common.model.ILocalizedDesc;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

/**
 * Defines the localizable contents of Point
 */
@Entity
public class PointDesc extends DescEntity<Point> {

    @NotNull
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copyDesc(ILocalizedDesc desc) {
        if (!(desc instanceof PointDesc)) {
            throw new IllegalArgumentException("Invalid desc class " + desc);
        }
        this.description = ((PointDesc)desc).getDescription();
    }
}
