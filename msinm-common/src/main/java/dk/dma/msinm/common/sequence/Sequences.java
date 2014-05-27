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
package dk.dma.msinm.common.sequence;

import org.slf4j.Logger;

import javax.ejb.*;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.Objects;

/**
 * Provides an interface for managing sequences
 */
@Singleton
public class Sequences {

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

        SequenceEntity seq = em.find(SequenceEntity.class, sequence.getSequenceName());
        if (seq != null) {
            // Increase the last value
            seq.setLastValue(seq.getLastValue() + 1);
            em.merge(seq);
            return seq.getLastValue();

        } else {
            seq = new SequenceEntity();
            seq.setName(sequence.getSequenceName());
            seq.setLastValue(sequence.initialValue());
            em.persist(seq);
            // NB: Return start value - not the next value.
            return seq.getLastValue();
        }
    }
}
