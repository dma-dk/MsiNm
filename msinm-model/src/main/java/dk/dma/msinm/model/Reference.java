package dk.dma.msinm.model;

import dk.dma.msinm.common.model.BaseEntity;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

/**
 * Defines a reference from one Message to another
 */
@Entity
public class Reference extends BaseEntity<Integer> {

    @ManyToOne
    Message message;

    @NotNull
    @Embedded
    SeriesIdentifier seriesIdentifier;

    ReferenceType type;

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public SeriesIdentifier getSeriesIdentifier() {
        return seriesIdentifier;
    }

    public void setSeriesIdentifier(SeriesIdentifier seriesIdentifier) {
        this.seriesIdentifier = seriesIdentifier;
    }

    public ReferenceType getType() {
        return type;
    }

    public void setType(ReferenceType type) {
        this.type = type;
    }
}
