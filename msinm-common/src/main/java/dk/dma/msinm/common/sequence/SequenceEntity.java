package dk.dma.msinm.common.sequence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Used internally by {@linkplain Sequences} to
 * manage sequences.
 */
@Entity
@Table(name="sequence")
public class SequenceEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    String name;
    long lastValue;

    @Id
    @Column(unique = true, nullable = false)
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getLastValue() { return lastValue; }
    public void setLastValue(long lastValue) { this.lastValue = lastValue; }
}