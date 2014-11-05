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

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dma.msinm.common.MsiNmApp;
import dk.dma.msinm.common.db.Sql;
import dk.dma.msinm.common.model.DataFilter;
import dk.dma.msinm.common.repo.RepoFileVo;
import dk.dma.msinm.common.repo.RepositoryService;
import dk.dma.msinm.common.sequence.DefaultSequence;
import dk.dma.msinm.common.sequence.Sequence;
import dk.dma.msinm.common.sequence.Sequences;
import dk.dma.msinm.common.service.BaseService;
import dk.dma.msinm.common.templates.TemplateContext;
import dk.dma.msinm.common.templates.TemplateService;
import dk.dma.msinm.common.templates.TemplateType;
import dk.dma.msinm.model.Area;
import dk.dma.msinm.model.Bookmark;
import dk.dma.msinm.model.Category;
import dk.dma.msinm.model.Chart;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.model.MessageDesc;
import dk.dma.msinm.model.MessageHistory;
import dk.dma.msinm.model.Reference;
import dk.dma.msinm.model.SeriesIdType;
import dk.dma.msinm.model.SeriesIdentifier;
import dk.dma.msinm.model.Status;
import dk.dma.msinm.model.Type;
import dk.dma.msinm.user.UserService;
import dk.dma.msinm.vo.MessageHistoryVo;
import dk.dma.msinm.vo.MessageVo;
import org.apache.commons.lang.StringUtils;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Topic;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    Logger log;

    @Resource
    SessionContext ctx;

    @Inject
    JMSContext jmsContext;

    @Resource(mappedName = "java:/jms/topic/messageTopic")
    Topic messageTopic;

    @Inject
    MessageCache messageCache;

    @Inject
    BookmarkCache bookmarkCache;

    @Inject
    RepositoryService repositoryService;

    @Inject
    UserService userService;

    @Inject
    Sequences sequences;

    @Inject
    TemplateService templateService;

    @Inject
    PublishingService publishingService;

    @Inject
    MsiNmApp app;

    @Inject
    @Sql("/sql/active_messages.sql")
    String selectActiveSql;

    /**
     * Creates or updates a Message without generating a message history entity
     *
     * @param message the message to create or update
     * @return the persisted message
     */
    public Message create(Message message) {
        log.info("Creating message " + message);
        return saveEntity(message);
    }

    /**
     * Creates a new message template with a temporary repository path
     *
     * @return the new message template
     */
    public MessageVo newTemplateMessage() {
        MessageVo messageVo = new MessageVo();
        messageVo.setValidFrom(new Date());
        messageVo.setLocations(new ArrayList<>());
        SeriesIdentifier id = new SeriesIdentifier();
        id.setAuthority(app.getOrganization());
        id.setMainType(SeriesIdType.MSI);
        id.setYear(Calendar.getInstance().get(Calendar.YEAR));
        messageVo.setSeriesIdentifier(id);
        messageVo.setType(Type.COASTAL_WARNING);
        messageVo.setRepoPath(repositoryService.getNewTempDir().getPath());
        publishingService.newTemplateMessage(messageVo);
        return messageVo;
    }


    /**
     * Saves the message and evicts the message from the cache
     *
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

        // Save a MessageHistory entity for the message
        saveHistory(message);

        return message;
    }

    /**
     * Creates a new message as a draft message
     *
     * @param messageVo the template for the message to create
     * @return the new message
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Message createMessage(MessageVo messageVo) throws Exception {

        Message message = messageVo.toEntity();

        // Validate the message
        SeriesIdentifier id = message.getSeriesIdentifier();
        if (message.getId() != null) {
            throw new Exception("Message already persisted");
        }
        if (id.getMainType() == null) {
            throw new Exception("Message main type must be specified");
        }
        if (message.getType() == null || message.getType().getSeriesIdType() != id.getMainType()) {
            throw new Exception("Missing or invalid Message type");
        }
        if (message.getValidFrom() == null) {
            throw new Exception("Message validFrom must be specified");
        }

        // Set default values
        if (StringUtils.isBlank(id.getAuthority())) {
            id.setAuthority(app.getOrganization());
        }
        if (id.getYear() == null) {
            id.setYear(Calendar.getInstance().get(Calendar.YEAR));
        }
        message.setStatus(Status.DRAFT);


        // Substitute the Area with a persisted one
        if (message.getArea() != null) {
            message.setArea(getByPrimaryKey(Area.class, message.getArea().getId()));
        }

        // Substitute the Categories with the persisted ones
        if (message.getCategories().size() > 0) {
            List<Category> categories = new ArrayList<>();
            message.getCategories().forEach(cat -> categories.add(getByPrimaryKey(Category.class, cat.getId())));
            message.setCategories(categories);
        }

        // Substitute the Charts with the persisted ones
        if (message.getCharts().size() > 0) {
            List<Chart> charts = new ArrayList<>();
            message.getCharts().forEach(chart -> charts.add(getByPrimaryKey(Chart.class, chart.getId())));
            message.setCharts(charts);
        }

        // Let publishers update the list of publications
        publishingService.createMessage(message);

        // Persist the message
        message = saveMessage(message);
        log.info("Saved message " + message);

        // Move the temporary repo folder to the final destination
        if (StringUtils.isNotBlank(messageVo.getRepoPath())) {
            String repoPath = repositoryService.getRepoPath(getMessageRepoFolder(message));
            log.info("Moving repo from " + messageVo.getRepoPath() + " to " + repoPath);
            boolean repoMoved = repositoryService.moveRepoFolder(messageVo.getRepoPath(), repoPath);

            // Update the description with the new path
            boolean descUpdated = false;
            if (repoMoved) {
                for (MessageDesc desc : message.getDescs()) {
                    if (StringUtils.isNotBlank(desc.getDescription()) && desc.getDescription().contains(messageVo.getRepoPath())) {
                        desc.setDescription(desc.getDescription().replaceAll(messageVo.getRepoPath(), repoPath));
                        descUpdated = true;
                    }
                }
                // Persist again if we have made any description changes
                if (descUpdated) {
                    message = saveMessage(message);
                }
            }
        }

        em.flush();
        return message;
    }

    /**
     * Updates the given message
     *
     * @param messageVo the template for the message to update
     * @return the updated message
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Message updateMessage(MessageVo messageVo) throws Exception {

        Message message = messageVo.toEntity();
        final Message original = getByPrimaryKey(Message.class, message.getId());

        // Validate the message
        SeriesIdentifier id = message.getSeriesIdentifier();
        if (message.getId() == null) {
            throw new Exception("Message not an existing message");
        }
        if (message.getType().getSeriesIdType() != id.getMainType()) {
            throw new Exception("Invalid Message type");
        }
        if (message.getValidFrom() == null) {
            throw new Exception("Message validFrom must be specified");
        }

        original.setSeriesIdentifier(message.getSeriesIdentifier());
        original.setType(message.getType());
        original.setCancellationDate(message.getCancellationDate());
        original.setHorizontalDatum(message.getHorizontalDatum());
        original.setStatus(message.getStatus());
        original.setOriginalInformation(message.isOriginalInformation());
        original.setPriority(message.getPriority());
        original.setValidFrom(message.getValidFrom());
        original.setValidTo(message.getValidTo());

        // Copy the area data
        original.copyDescsAndRemoveBlanks(message.getDescs());

        // Add the locations
        original.getLocations().clear();
        original.getLocations().addAll(message.getLocations());

        // Add the light list numbers
        original.getLightsListNumbers().clear();
        original.getLightsListNumbers().addAll(message.getLightsListNumbers());

        // Add the references
        original.getReferences().clear();
        message.getReferences().forEach(ref -> original.getReferences().add(ref.isNew() ? ref : getByPrimaryKey(Reference.class, ref.getId())));

        // Copy the Area
        original.setArea(null);
        if (message.getArea() != null) {
            original.setArea(getByPrimaryKey(Area.class, message.getArea().getId()));
        }

        // Copy the Categories
        original.getCategories().clear();
        message.getCategories().forEach(cat -> original.getCategories().add(getByPrimaryKey(Category.class, cat.getId())));

        // Substitute the Charts with the persisted ones
        original.getCharts().clear();
        message.getCharts().forEach(chart -> original.getCharts().add(getByPrimaryKey(Chart.class, chart.getId())));

        // Update the publications
        final Message msg = message;
        original.getPublications().forEach(pub -> pub.copyData(msg.getPublication(pub.getType())));
        // And let publishers have a say
        publishingService.updateMessage(message);

        // Persist the message
        message = saveMessage(original);
        log.info("Updated message " + message);

        em.flush();
        return message;
    }

    /**
     * Updates the status of the given message
     *
     * @param messageId the id of the message
     * @param status    the status
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Message setStatus(Integer messageId, Status status) {
        Message msg = getByPrimaryKey(Message.class, messageId);

        // TODO: Proper publishing
        if (msg.getStatus() == Status.DRAFT && status == Status.PUBLISHED) {
            int number = newSeriesIdentifierNumber(
                    msg.getType(),
                    msg.getSeriesIdentifier().getAuthority(),
                    msg.getSeriesIdentifier().getYear());
            msg.getSeriesIdentifier().setNumber(number);
        }

        msg.setStatus(status);

        // And let publishers have a say
        publishingService.setStatus(msg);

        msg = saveMessage(msg);

        // Broadcast the update
        sendStatusUpdate(msg);

        return msg;
    }

    /**
     * Broadcasts a JMS message to indicate that the message status has changed
     * @param message the message
     */
    public void sendStatusUpdate(Message message) {
        Map<String, Object> body = new HashMap<>();
        body.put("ID", message.getId());
        body.put("STATUS", message.getStatus().name());
        try {
            jmsContext.createProducer().send(messageTopic, body);
        } catch (Exception e) {
            log.error("Failed sending JMS: " + e, e);
        }
    }

    /**
     * Returns all messages
     *
     * @return all messages
     */
    @RolesAllowed({"user"})
    public List<Message> getAll() {
        return getAll(Message.class);
    }

    /**
     * Returns all messages
     *
     * @return all messages
     */
    @RolesAllowed({"user"})
    public List<Message> getActive() {
        return em.createQuery(selectActiveSql, Message.class)
                .getResultList();
    }

    /**
     * Returns the message with the given id
     *
     * @param id the id of the message
     * @return the message with the given id or null if not found
     */
    public Message findById(Integer id) {
        return getByPrimaryKey(Message.class, id);
    }

    /**
     * Finds the message by the given message series values
     *
     * @param type             the type
     * @param messageNumber    the message number
     * @param messageYear      the message year
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
     * Returns the last updated time of messages that has been published at any point of time
     * @return the last updated time
     */
    public Date findLastUpdated() {
        try {
            return em.createNamedQuery("Message.findLastUpdatedWithStatus", Date.class)
                    .setParameter("statusList", EnumSet.of(Status.PUBLISHED, Status.CANCELLED, Status.EXPIRED))
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Creates a new series identifier number specific for the given type, authority and year
     *
     * @param type      the message type
     * @param authority the authority
     * @param year      the year
     * @return the new series identifier number
     */
    public int newSeriesIdentifierNumber(Type type, String authority, int year) {
        Sequence sequence = new DefaultSequence("MESSAGE_SERIES_ID_" + type.getPrefix() + "_" + authority + "_" + year, 1);
        return (int) sequences.getNextValue(sequence);
    }

    /**
     * Creates a new series identifier specific for the given type, authority and year
     *
     * @param type      the message type
     * @param authority the authority
     * @param year      the year
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
     *
     * @param date     the date
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
     * Returns all published messages that have the given category or sub-categories of the given category
     *
     * @param category the category
     * @return all published messages with the given category
     */
    public List<Message> findPublishedMessagesByCategory(Category category) {
        return em
                .createNamedQuery("Message.findActiveByCategory", Message.class)
                .setParameter("lineage", category.getLineage() + "%")
                .getResultList();
    }

    /**
     * Returns all published messages that have expired
     *
     * @return all published messages that have expired
     */
    public List<Message> findPublishedExpiredMessages() {
        return em
                .createNamedQuery("Message.findPublishedExpiredMessages", Message.class)
                .getResultList();
    }

    /**
     * Inactivate all active P&T NM messages created before the given date, excluding the given noticeIds
     *
     * @param noticeIds the P&T NM notices that should no be inactivated
     * @param date      the date
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
                    saveMessage(msg);
                    deactivated.add(msg);
                });

        return deactivated;
    }

    /**
     * Generates content based on the Freemarker template
     *
     * @param messageVo the message to use in the Freemarker template
     * @param template  the Freemarker template
     * @param language  the language
     * @return the result
     */
    public String transformMessage(MessageVo messageVo, String template, String language) throws Exception {

        long t0 = System.currentTimeMillis();

        // Sort all descs by language
        messageVo.sortDescsByLang(language);

        // Create a freemarker template for the transformation
        Map<String, Object> data = new HashMap<>();
        data.put("message", messageVo);

        TemplateContext ctx = templateService.getTemplateContext(
                TemplateType.MESSAGE,
                template,
                data,
                app.getLanguage(language),
                "Message");
        try {

            // Process the template
            String result = templateService.process(ctx);
            log.info("Transformed message using " + template + " in " + (System.currentTimeMillis() - t0) + " ms");
            return result;

        } catch (Exception e) {
            log.error("Error transforming message using template " + template, e);
            throw e;
        }
    }

    /***************************************/
    /** Externalizing html links          **/
    /***************************************/

    /**
     * Utility method that will process the HTML and turn all images and links into
     * absolute URL's pointing back to the MSI-NM server.
     * @param html the HTML to process
     * @return the processed HTML
     */
    public String externalizeHtml(String html) {
        if (StringUtils.isNotBlank(html)) {
            Document doc = Jsoup.parse(html, app.getBaseUri());
            externalizeLinks(doc, "a", "href");
            externalizeLinks(doc, "img", "src");
            html = doc.toString();
        }
        return html;
    }

    /**
     * Make sure that links are absolute.
     * @param doc the HTML document
     */
    protected void externalizeLinks(Document doc, String tag, String attr) {
        Elements elms = doc.select(tag + "[" + attr + "]");
        for (Element e : elms) {

            String url = e.absUrl(attr);
            if (url.length() == 0) {
                // Disable link
                e.attr(attr, "#");
                continue;
            }
            // Update the link to be the absolute link
            e.attr(attr, url);
        }
    }

    /***************************************/
    /** Message History methods           **/
    /***************************************/

    /**
     * Saves a history entity containing a snapshot of the message
     *
     * @param message the message to save a snapshot for
     */
    public void saveHistory(Message message) {

        try {
            MessageHistory hist = new MessageHistory();
            hist.setMessage(message);
            hist.setStatus(message.getStatus());
            hist.setCreated(new Date());
            hist.setVersion(message.getVersion() + 1);

            // The user may not be defined, say, if this is a legacy import
            if (ctx != null && ctx.getCallerPrincipal() != null) {
                hist.setUser(userService.findByPrincipal(ctx.getCallerPrincipal()));
            }

            // Create a snapshot of the message
            ObjectMapper jsonMapper = new ObjectMapper();
            MessageVo snapshot = new MessageVo(message, DataFilter.get("Message.details"));
            hist.setSnapshot(jsonMapper.writeValueAsString(snapshot));

            saveEntity(hist);
        } catch (Exception e) {
            log.error("Error saving a history entry for message " + message.getId(), e);
            // NB: We do not propagate the error, since we do not want to prevent
            //    the original message operation
        }
    }

    /**
     * Returns the message history for the given message ID
     *
     * @param messageId the message ID
     * @return the message history
     */
    public List<MessageHistoryVo> getMessageHistory(Integer messageId) {
        return em.createNamedQuery("MessageHistory.findByMessageId", MessageHistory.class)
                .setParameter("messageId", messageId)
                .getResultList()
                .stream()
                .map(MessageHistoryVo::new)
                .collect(Collectors.toList());
    }


    /***************************************/
    /** Bookmarks methods                 **/
    /***************************************/

    /**
     * Returns the bookmarks for the calling user.
     *
     * @return the bookmarks for the calling user
     */
    public Set<Integer> getBookmarks() {
        // The user may not be defined
        if (ctx != null && ctx.getCallerPrincipal() != null) {
            String email = ctx.getCallerPrincipal().getName();
            Set<Integer> bookmarkIds = bookmarkCache.getCache().get(email);
            if (bookmarkIds == null) {
                bookmarkIds = em.createNamedQuery("Bookmark.findByUserEmail", Bookmark.class)
                        .setParameter("email", email)
                        .getResultList()
                        .stream()
                        .map(bookmark -> bookmark.getMessage().getId())
                        .collect(Collectors.toSet());
                bookmarkCache.getCache().put(email, bookmarkIds);
            }
            return bookmarkIds;
        }
        return new HashSet<>();
    }

    /**
     * Adds a bookmark for the calling user
     *
     * @param messageId the id of the message
     * @return if the bookmark was added
     */
    public boolean addBookmark(Integer messageId) {
        // Only add bookmarks for a calling user
        if (ctx == null || ctx.getCallerPrincipal() == null || messageId == null) {
            return false;
        }
        String email = ctx.getCallerPrincipal().getName();

        Set<Integer> bookmarkIds = getBookmarks();
        // Only add the bookmark once
        if (bookmarkIds.contains(messageId)) {
            return false;
        }

        // Create the new bookmark. Throws an exception if the messageId is invalid...
        Bookmark bookmark = new Bookmark();
        bookmark.setUser(userService.findByPrincipal(ctx.getCallerPrincipal()));
        bookmark.setMessage(findById(messageId));
        saveEntity(bookmark);
        log.info("Added bookmark for user=" + email + ", messageId=" + messageId);

        // Flush the cache
        bookmarkCache.getCache().remove(email);
        return true;
    }

    /**
     * Removes a bookmark for the calling user
     *
     * @param messageId the id of the message
     * @return if the bookmark was removed
     */
    public boolean removeBookmark(Integer messageId) {
        // Only add bookmarks for a calling user
        if (ctx == null || ctx.getCallerPrincipal() == null || messageId == null) {
            return false;
        }
        String email = ctx.getCallerPrincipal().getName();

        // Check if the bookmark is cached at all
        Set<Integer> bookmarkIds = getBookmarks();
        if (!bookmarkIds.contains(messageId)) {
            return false;
        }

        // Look up the bookmark
        Bookmark bookmark = em.createNamedQuery("Bookmark.findByUserEmailAndMessageId", Bookmark.class)
                .setParameter("email", email)
                .setParameter("messageId", messageId)
                .getSingleResult();

        // Purge it
        em.remove(bookmark);
        log.info("Removed bookmark for user=" + email + ", messageId=" + messageId);

        // Flush the cache
        bookmarkCache.getCache().remove(email);
        return true;
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
     * Returns the repository URI for the message folder
     * @param message the message
     * @return the associated repository URI
     */
    public String getMessageFolderRepoPath(Message message) throws IOException {
        return repositoryService.getRepoPath(getMessageRepoFolder(message));
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

    /**
     * Returns the list of repository attachments for the message
     * @param id the ID of the message to find the attachments for
     * @return the attachments
     */
    public List<RepoFileVo> getMessageAttacthments(Integer id) throws IOException {
        // Look up the attachments associated with the message
        Path reportFolder = getMessageRepoFolder(id);
        String uri = repositoryService.getRepoPath(reportFolder);
        return repositoryService.listFiles(uri);
    }

    /**
     * Returns the list of repository attachments for the message
     * @param message the message to find the attachments for
     * @return the attachments
     */
    public List<RepoFileVo> getMessageAttacthments(Message message) throws IOException {
        return getMessageAttacthments(message.getId());
    }
}
