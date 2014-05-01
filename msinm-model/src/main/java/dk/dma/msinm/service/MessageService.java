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
package dk.dma.msinm.service;

import dk.dma.msinm.common.db.Sql;
import dk.dma.msinm.common.service.BaseService;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.model.NavwarnMessage;
import dk.dma.msinm.model.NoticeMessage;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import java.util.Date;
import java.util.List;

/**
 * Business interface for accessing MSI-NM messages
 */
@Stateless
public class MessageService extends BaseService {

    @Inject
    private Logger log;

    @Inject
    @Sql("/sql/active_messages.sql")
    private String selectActiveSql;

    /**
     * Creates or updates a NavwarnMessage
     * @param navwarnMessage the message to create or update
     * @return the persisted message
     */
    public NavwarnMessage create(NavwarnMessage navwarnMessage) {
        log.info("Creating navwarn message");
        return saveEntity(navwarnMessage);
    }

    /**
     * Creates or updates a NoticeMessage
     * @param noticeMessage the message to create or update
     * @return the persisted message
     */
    public NoticeMessage create(NoticeMessage noticeMessage) {
        log.info("Creating notice message");
        return saveEntity(noticeMessage);
    }

    /**
     * Returns all messages
     * @return all messages
     */
    public List<Message> getAll() {
        return getAll(Message.class);
    }

    /**
     * Returns all messages
     * @return all messages
     */
    public List<Message> getActive() {
        return em.createQuery(selectActiveSql, Message.class)
                .getResultList();
    }

    /**
     * Returns the message with the given id
     * @param id the id of the message
     * @return the message with the given id or null if not found
     */
    public Message findById(Integer id) {
        return getByPrimaryKey(Message.class, id);
    }

    /**
     * Finds the message by the given message series values
     *
     * @param messageNumber the message number
     * @param messageYear the message year
     * @param messageAuthority the message authority
     * @return the message or null if not found
     */
    public Message findByMessageSeriesId(int messageNumber, int messageYear, String messageAuthority) {
        // Execute and return the result
        try {
            return em
                    .createNamedQuery("Message.findBySeriesIdentifier", Message.class)
                    .setParameter("number", messageNumber)
                    .setParameter("year", messageYear)
                    .setParameter("authority", messageAuthority)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    /**
     * Returns all messages updated after the given date
     * @param date the date
     * @param maxCount the max number of entries to return
     * @return all messages updated after the given date
     */
    public List<Message> findUpdatedMessages(Date date, int maxCount) {
        return em
                .createNamedQuery("Message.findUpdateMessages", Message.class)
                .setParameter("date", date)
                .setMaxResults(maxCount)
                .getResultList();
    }
}
