package dk.dma.msinm.model;

import dk.dma.msinm.common.model.BaseEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

/**
 * Entity class for all Message publications, such as Mail, Navtex, Twitter, etc.
 * <p>
 *     About the design: Because of the nightmarish JAX-RS handling of polymorphic
 *     JSON unmarshalling, custom data is stored in the "data" property rather that
 *     using sub-classes of the Publication entity for each type.
 *     <br/>
 *     This is acceptable because we do not want to e.g. index the publication data.
 * </p>
 */
@Entity
public class Publication extends BaseEntity<Integer> {

    @NotNull
    @ManyToOne(cascade = CascadeType.ALL)
    Message message;

    @NotNull
    String type;

    boolean publish;

    @Lob
    String data;

    /**
     * Copy the data of the publication
     * @param publication the data to copy
     */
    public void copyData(Publication publication) {
        if (publication != null) {
            type = publication.getType();
            publish = publication.isPublish();
            data = publication.getData();
        }
    }

    // ****** Getters and setters ***********

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isPublish() {
        return publish;
    }

    public void setPublish(boolean publish) {
        this.publish = publish;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
