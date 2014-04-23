/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.msinm.common.service;

import dk.dma.msinm.common.model.BaseEntity;
import dk.dma.msinm.common.model.Sequence;
import org.slf4j.Logger;

import javax.ejb.*;
import javax.inject.Inject;
import javax.persistence.*;
import java.util.Objects;

/**
 * Provides an interface for managing sequences
 */
@Singleton
public class SequenceService  {

    @Inject
    private Logger log;

    @Inject
    protected EntityManager em;

    /**
     * Returns the next value of the given sequence
     * @param sequence the sequence
     * @return the next value
     */
    @Lock(LockType.WRITE)
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public long getNextValue(Sequence sequence) {
        Objects.requireNonNull(sequence, "No sequence specified");
        Objects.requireNonNull(sequence.getSequenceName(), "Invalid sequence specified");

        // No-group case
        try {
            DbSequence seq = em.createNamedQuery("DbSequence.findByName", DbSequence.class)
                    .setParameter("name", sequence.getSequenceName())
                    .getSingleResult();
            return getNextValue(seq);
        } catch (NoResultException ex) {
            DbSequence seq = new DbSequence();
            seq.setName(sequence.getSequenceName());
            seq.setLastValue(sequence.initialValue());
            em.persist(seq);
            // NB: Return start value - not the next value.
            return seq.getLastValue();
        }
    }

    /**
     * Updates the sequence and returns the next value
     * @param seq the sequence
     * @return the next value
     */
    private long getNextValue(DbSequence seq) {
        // Increase the last value
        seq.setLastValue(seq.getLastValue() + 1);
        em.merge(seq);
        return seq.getLastValue();
    }
}

/**
 * Used internally by {@linkplain dk.dma.msinm.common.service.SequenceService} to
 * manage sequences.
 */
@Entity
@Table(name="sequence")
@NamedQueries({
        @NamedQuery(name= "DbSequence.findByName",
                query="SELECT s FROM DbSequence as s where s.name = :name")
})
class DbSequence  extends BaseEntity<Integer> {

    private static final long serialVersionUID = 1L;
    String name;
    long lastValue;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getLastValue() { return lastValue; }
    public void setLastValue(long lastValue) { this.lastValue = lastValue; }
}