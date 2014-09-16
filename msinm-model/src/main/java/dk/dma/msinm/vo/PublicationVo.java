package dk.dma.msinm.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dk.dma.msinm.common.vo.BaseVo;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.model.Publication;

import java.util.Set;

/**
 * Value object for the {@code Publication} model entity.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class PublicationVo extends BaseVo<Publication> {

    Integer id;
    String type;
    boolean publish;
    String data;
    Set<String> messageTypes;

    /**
     * Constructor
     */
    public PublicationVo() {
    }

    /**
     * Constructor
     * @param publication the publication entity
     */
    public PublicationVo(Publication publication) {
        super(publication);
        id = publication.getId();
        type = publication.getType();
        publish = publication.isPublish();
        data = publication.getData();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Publication toEntity() {
        Publication publication = new Publication();
        publication.setId(id);
        publication.setType(type);
        publication.setPublish(publish);
        publication.setData(data);
        return publication;
    }

    public Publication toEntity(Message message) {
        Publication publication = toEntity();
        publication.setMessage(message);
        return publication;
    }

    //*********** Getters and setters *************

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Set<String> getMessageTypes() {
        return messageTypes;
    }

    public void setMessageTypes(Set<String> messageTypes) {
        this.messageTypes = messageTypes;
    }
}
