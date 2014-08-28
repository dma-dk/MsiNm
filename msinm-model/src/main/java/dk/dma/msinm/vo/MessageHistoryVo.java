package dk.dma.msinm.vo;

import dk.dma.msinm.common.vo.BaseVo;
import dk.dma.msinm.model.MessageHistory;
import dk.dma.msinm.model.Status;

import java.util.Date;

/**
 * Value object for the {@code MessageHistory} class
 */
public class MessageHistoryVo extends BaseVo<MessageHistory> {

    Integer messageId;
    Status status;
    String user;
    int version;
    Date created;
    String snapshot;

    /**
     * Constructor
     */
    public MessageHistoryVo() {
    }

    /**
     * Constructor
     * @param entity the MessageHistory to create the VO for
     */
    public MessageHistoryVo(MessageHistory entity) {
        super(entity);

        messageId = entity.getMessage().getId();
        status = entity.getStatus();
        user = (entity.getUser()) != null ? entity.getUser().getEmail() : null;
        version = entity.getVersion();
        created = entity.getCreated();
        snapshot = entity.getSnapshot();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageHistory toEntity() {
        // Not used
        return null;
    }

    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
    }
}
