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
import dk.dma.msinm.common.model.DataFilter;
import dk.dma.msinm.common.repo.RepositoryService;
import dk.dma.msinm.common.sequence.DefaultSequence;
import dk.dma.msinm.common.sequence.Sequence;
import dk.dma.msinm.common.sequence.Sequences;
import dk.dma.msinm.common.service.BaseService;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.model.SeriesIdType;
import dk.dma.msinm.model.SeriesIdentifier;
import dk.dma.msinm.model.Status;
import dk.dma.msinm.model.Type;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.slf4j.Logger;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Business interface for accessing MSI-NM messages
 */
@Stateless
@SecurityDomain("msinm-policy")
@PermitAll
public class MessageService extends BaseService {

    public static String MESSAGE_REPO_FOLDER = "messages";
    public static final DataFilter CACHED_MESSAGE_DATA = DataFilter.get("Message.details", "Area.parent", "Category.parent");

    @Inject
    private Logger log;

    @Inject
    private MessageCache messageCache;

    @Inject
    private RepositoryService repositoryService;

    @Inject
    private Sequences sequences;

    @Inject
    @Sql("/sql/active_messages.sql")
    private String selectActiveSql;

    /**
     * Creates or updates a Message
     * @param message the message to create or update
     * @return the persisted message
     */
    public Message create(Message message) {
        log.info("Creating message " + message);
        return saveEntity(message);
    }

    /**
     * Saves the message and evicts the message from the cache
     * @param message the message to save
     * @return the saved message
     */
    public Message saveMessage(Message message) {
        boolean wasPersisted = message.isPersisted();

        // Save the message
        message = saveEntity(message);

        // If it is not a new message, evict it from the message cache
        if (wasPersisted) {
            evictCachedMessage(message);
        }

        return message;
    }

    /**
     * Updates the status of the given message
     * @param msg the message
     * @param status the status
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void setStatus(Message msg, Status status) {
        msg = getByPrimaryKey(Message.class, msg.getId());
        msg.setStatus(status);
        saveEntity(msg);

        // Un-cache the message
        evictCachedMessage(msg);
    }

    /**
     * Returns all messages
     * @return all messages
     */
    @RolesAllowed({ "user" })
    public List<Message> getAll() {
        return getAll(Message.class);
    }

    /**
     * Returns all messages
     * @return all messages
     */
    @RolesAllowed({ "user" })
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
     * @param type the type
     * @param messageNumber the message number
     * @param messageYear the message year
     * @param messageAuthority the message authority
     * @return the message or null if not found
     */
    public Message findBySeriesIdentifier(SeriesIdType type, int messageNumber, int messageYear, String messageAuthority) {
        // Execute and return the result
        try {
            return em
                    .createNamedQuery("Message.findBySeriesIdentifier", Message.class)
                    .setParameter("type", type)
                    .setParameter("number", messageNumber)
                    .setParameter("year", messageYear)
                    .setParameter("authority", messageAuthority)
                    .getSingleResult();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Finds the message by the given message series values
     *
     * @param seriesIdentifier the series id with the format type-authority-number-year
     * @return the message or null if not found
     */
    public Message findBySeriesIdentifier(String seriesIdentifier) {
        try {
            String[] parts = seriesIdentifier.split("-");
            return findBySeriesIdentifier(
                    SeriesIdType.valueOf(parts[0].toUpperCase()),
                    Integer.parseInt(parts[2]),
                    2000 + Integer.parseInt(parts[3]),
                    parts[1]);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Creates a new series identifier number specific for the given type, authority and year
     * @param type the message type
     * @param authority the authority
     * @param year the year
     * @return the new series identifier number
     */
    public int newSeriesIdentifierNumber(Type type, String authority, int year) {
        Sequence sequence = new DefaultSequence("MESSAGE_SERIES_ID_" + type.getPrefix() + "_" + authority + "_" + year, 0);
        return (int)sequences.getNextValue(sequence);
    }

    /**
     * Creates a new series identifier specific for the given type, authority and year
     * @param type the message type
     * @param authority the authority
     * @param year the year
     * @return the new series identifier
     */
    public SeriesIdentifier newSeriesIdentifier(Type type, String authority, int year) {
        SeriesIdentifier identifier = new SeriesIdentifier();
        identifier.setMainType(type.getSeriesIdType());
        identifier.setAuthority(authority);
        identifier.setYear(year);
        identifier.setNumber(newSeriesIdentifierNumber(type, authority, year));
        return identifier;
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

    /**
     * Returns all published messages that have expired
     * @return all published messages that have expired
     */
    public List<Message> findPublishedExpiredMessages() {
        return em
                .createNamedQuery("Message.findPublishedExpiredMessages", Message.class)
                .getResultList();
    }

    /**
     * Inactivate all active P&T NM messages created before the given date, excluding the given noticeIds
     * @param noticeIds the P&T NM notices that should no be inactivated
     * @param date the date
     * @return the messages actually deactivated
     */
    public List<Message> inactivateTempPrelimNmMessages(List<SeriesIdentifier> noticeIds, Date date) {
        List<Message> messages =
                em.createNamedQuery("Message.findActiveNotices", Message.class)
                        .setParameter("date", date)
                        .getResultList();
        List<Message> deactivated = new ArrayList<>();
        Set<SeriesIdentifier> excludeIds = new HashSet<>();
        excludeIds.addAll(noticeIds);

        messages.stream()
                .filter(msg -> !excludeIds.contains(msg.getSeriesIdentifier()))
                .forEach(msg -> {
                    msg.setValidTo(date);
                    msg.setStatus(Status.CANCELLED);
                    saveEntity(msg);
                    evictCachedMessage(msg);
                    deactivated.add(msg);
                });

        return deactivated;
    }

    /***************************************/
    /** Cache methods                     **/
    /***************************************/

    /**
     * Fetches and caches the message with the given id.
     * Related data structures, such as locations are pre-fetched for the message
     * @param id the id of the message
     * @return the cached message
     */
    public Message getCachedMessage(Integer id) {
        Message message = messageCache.getCache().get(id);
        if (message == null) {
            message = findById(id);
            if (message != null) {
                message.preload(CACHED_MESSAGE_DATA);
                em.detach(message);
                messageCache.getCache().put(id, message);
            }
        }
        return message;
    }

    /**
     * Fetches and caches the messages with the given ids.
     * Related data structures, such as locations are pre-fetched for the message
     * @param ids the id of the message
     * @return the cached messages
     */
    public List<Message> getCachedMessages(List<Integer> ids) {
        List<Message> messages = new ArrayList<>();
        ids.forEach(id -> {
            Message message = getCachedMessage(id);
            if (message != null) {
                messages.add(message);
            }
        });
        return messages;
    }

    /**
     * Evicts the message with the given id from the cache
     * @param id the id of the message to evict
     */
    public void evictCachedMessageId(Integer id) {
        if (id != null) {
            messageCache.getCache().remove(id);
        }
    }

    /**
     * Evicts the messages with the given ids from the cache
     * @param ids the ids of the messages to evict
     */
    public void evictCachedMessageIds(List<Integer> ids) {
        if (ids != null) {
            ids.forEach(this::evictCachedMessageId);
        }
    }

    /**
     *
     * Evicts the message from the cache
     * @param message the message to evict
     */
    public void evictCachedMessage(Message message) {
        if (message != null) {
            evictCachedMessageId(message.getId());
        }
    }

    /**
     * Evicts the messages from the cache
     * @param messages the messages to evict
     */
    public void evictCachedMessages(List<Message> messages) {
        if (messages != null) {
            messages.forEach(this::evictCachedMessage);
        }
    }

    /***************************************/
    /** Repo methods                      **/
    /***************************************/

    /**
     * Returns the repository folder for the given message
     * @param id the id of the message
     * @return the associated repository folder
     */
    public Path getMessageRepoFolder(Integer id) throws IOException {
        return  repositoryService.getHashedSubfolder(MESSAGE_REPO_FOLDER, String.valueOf(id), true);
    }

    /**
     * Returns the repository folder for the given message
     * @param message the message
     * @return the associated repository folder
     */
    public Path getMessageRepoFolder(Message message) throws IOException {
        return  getMessageRepoFolder(message.getId());
    }

    /**
     * Returns the repository file for the given message file
     * @param message the message
     * @param name the file name
     * @return the associated repository file
     */
    public Path getMessageFileRepoPath(Message message, String name) throws IOException {
        return  getMessageRepoFolder(message).resolve(name);
    }

    /**
     * Returns the repository URI for the given message file
     * @param message the message
     * @param name the file name
     * @return the associated repository URI
     */
    public String getMessageFileRepoUri(Message message, String name) throws IOException {
        Path file = getMessageRepoFolder(message).resolve(name);
        return repositoryService.getRepoUri(file);
    }

}
