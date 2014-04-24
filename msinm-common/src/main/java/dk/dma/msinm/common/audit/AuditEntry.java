package dk.dma.msinm.common.audit;

import dk.dma.msinm.common.model.BaseEntity;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Used for audit logging
 */
@Entity
@Table(name="audit")
public class AuditEntry extends BaseEntity<Long> {

    // The audit level
    public enum Level {
        OK, ERROR
    }

    @Column(length = 100)
    private String module;

    private Level level;

    @Column(length = 200)
    private String message;

    @Column(length = 4000)
    private String stackTrace;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime created;

    @Override
    public String toString() {
        return "AuditEntry{" +
                "module='" + module + '\'' +
                ", level=" + level +
                ", message='" + message + '\'' +
                ", created=" + created +
                '}';
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public DateTime getCreated() {
        return created;
    }

    public void setCreated(DateTime created) {
        this.created = created;
    }
}
