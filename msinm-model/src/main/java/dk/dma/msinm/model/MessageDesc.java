package dk.dma.msinm.model;

import dk.dma.msinm.common.model.DescEntity;
import dk.dma.msinm.common.model.ILocalizedDesc;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;

/**
 * Localized contents for the Message entity
 */
@Entity
public class MessageDesc extends DescEntity<Message>  {

    @Column(length = 1000)
    String title;

    @Lob
    String description;

    String otherCategories;

    @Column(length = 1000)
    String time;

    String vicinity;

    /**
     * {@inheritDoc}
     */
    @Override
    public void copyDesc(ILocalizedDesc desc) {
        if (!(desc instanceof MessageDesc)) {
            throw new IllegalArgumentException("Invalid desc class " + desc);
        }
        MessageDesc other = (MessageDesc)desc;
        this.title = other.getTitle();
        this.description = other.getDescription();
        this.otherCategories = other.getOtherCategories();
        this.time = other.getTime();
        this.vicinity = other.getVicinity();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOtherCategories() {
        return otherCategories;
    }

    public void setOtherCategories(String otherCategories) {
        this.otherCategories = otherCategories;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }
}