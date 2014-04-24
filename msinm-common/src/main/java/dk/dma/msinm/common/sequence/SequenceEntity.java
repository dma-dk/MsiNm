package dk.dma.msinm.common.sequence;

import dk.dma.msinm.common.model.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Used internally by {@linkplain dk.dma.msinm.common.sequence.SequenceService} to
 * manage sequences.
 */
@Entity
@Table(name="sequence")
@NamedQueries({
        @NamedQuery(name= "SequenceEntity.findByName",
                query="SELECT s FROM SequenceEntity as s where s.name = :name")
})
public class SequenceEntity extends BaseEntity<Integer> {

    private static final long serialVersionUID = 1L;

    String name;
    long lastValue;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getLastValue() { return lastValue; }
    public void setLastValue(long lastValue) { this.lastValue = lastValue; }
}