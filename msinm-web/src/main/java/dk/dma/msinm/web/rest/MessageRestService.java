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
package dk.dma.msinm.web.rest;

import dk.dma.msinm.common.MsiNmApp;
import dk.dma.msinm.common.model.DataFilter;
import dk.dma.msinm.common.repo.RepoFileVo;
import dk.dma.msinm.common.templates.PdfService;
import dk.dma.msinm.common.time.TimeModel;
import dk.dma.msinm.common.time.TimeParser;
import dk.dma.msinm.common.time.TimeProcessor;
import dk.dma.msinm.common.time.TimeTranslator;
import dk.dma.msinm.common.vo.JsonSerializable;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.model.Reference;
import dk.dma.msinm.model.ReferenceType;
import dk.dma.msinm.model.SeriesIdentifier;
import dk.dma.msinm.model.Status;
import dk.dma.msinm.service.CalendarService;
import dk.dma.msinm.service.CategoryService;
import dk.dma.msinm.service.MessageSearchParams;
import dk.dma.msinm.service.MessageSearchResult;
import dk.dma.msinm.service.MessageSearchService;
import dk.dma.msinm.service.MessageService;
import dk.dma.msinm.service.PublishingService;
import dk.dma.msinm.vo.MessageHistoryVo;
import dk.dma.msinm.vo.MessageVo;
import dk.dma.msinm.vo.ReferenceVo;
import org.apache.commons.lang.StringUtils;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static dk.dma.msinm.service.Publisher.PublisherContext;

/**
 * REST interface for accessing MSI-NM messages
 */
@Path("/messages")
@Stateless
@SecurityDomain("msinm-policy")
@PermitAll
public class MessageRestService {

    public static final String TYPE_ICALENDAR = "text/calendar;charset=UTF-8";

    @Inject
    Logger log;

    @Inject
    MessageService messageService;

    @Inject
    CategoryService categoryService;

    @Inject
    MessageSearchService messageSearchService;

    @Inject
    PdfService pdfService;

    @Inject
    CalendarService calendarService;

    @Inject
    PublishingService publishingService;

    @Inject
    MsiNmApp app;

    public MessageRestService() {
    }


    /**
     * Creates a new message template with a temporary repository path
     *
     * @return the new message template
     */
    @GET
    @Path("/new-message-template")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    @RolesAllowed({"editor"})
    public MessageVo newTemplateMessage() {
        return messageService.newTemplateMessage();
    }

    /**
     * Creates a new message copy template with a temporary repository path
     *
     * @return the new message copy template
     */
    @GET
    @Path("/copy-message-template/{messageId}")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    @RolesAllowed({"editor"})
    public MessageVo copyMessageTemplate(@PathParam("messageId") String messageId, @QueryParam("reference") String reference) {
        // Look up the message
        final MessageVo message = getMessage(messageId, null);

        // Check if we need to add the original message as a reference
        if (StringUtils.isNotBlank(reference)) {
            try {
                if (message.getReferences() == null) {
                    message.setReferences(new HashSet<>());
                }
                Reference ref = new Reference();
                ref.setSeriesIdentifier(message.getSeriesIdentifier().copy());
                ref.setType(ReferenceType.valueOf(reference));
                message.getReferences().add(new ReferenceVo(ref));

            } catch (IllegalArgumentException e) {
                log.warn("Failed to add reference of type " + reference);
            }
        }

        // Get a temp repo path
        MessageVo newTemplateMessage = newTemplateMessage();
        message.setRepoPath(newTemplateMessage.getRepoPath());

        // Reset various identifier fields
        message.setId(null);
        message.setStatus(Status.DRAFT);
        SeriesIdentifier id = new SeriesIdentifier();

        id.setMainType(message.getSeriesIdentifier().getMainType());
        id.setAuthority(message.getSeriesIdentifier().getAuthority());
        id.setYear(newTemplateMessage.getSeriesIdentifier().getYear());
        message.setSeriesIdentifier(id);
        if (message.getReferences() != null) {
            message.getReferences().forEach(ref -> ref.setId(null));
        }
        if (message.getPublications() != null) {
            message.getPublications().forEach(pub -> pub.setId(null));
        }
        if (newTemplateMessage.getPublications() != null) {
            newTemplateMessage.getPublications().forEach(message::addPublicationIfUndefined);
        }

        return message;
    }

    /**
     * Translates the messageId, which may be an ID or a series identifier, into a message id
     *
     * @param messageId the mesage id
     * @return the message id
     */
    private Integer getMessageId(String messageId) {
        // Sanity check
        if (messageId == null) {
            throw new IllegalArgumentException("Must specify message id");
        }

        // The message id is either the ID of the message or the message series identifier
        Integer id = null;
        if (StringUtils.isNumeric(messageId)) {
            id = Integer.valueOf(messageId);
        } else {
            Message message = messageService.findBySeriesIdentifier(messageId);
            if (message != null) {
                id = message.getId();
            }
        }
        return id;
    }

    /**
     * Returns the message with the given ID or series ID
     *
     * @return the message, or null if not found
     */
    @GET
    @Path("/message/{messageId}")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    public MessageVo getMessage(@PathParam("messageId") String messageId, @QueryParam("lang") String lang) {

        // Get the message id
        Integer id = getMessageId(messageId);

        // Look up the cached message with the computed id
        MessageVo result = null;
        if (id != null) {
            Message message = messageService.getCachedMessage(id);
            if (message != null) {
                DataFilter filter = new DataFilter(MessageService.CACHED_MESSAGE_DATA).setLang(lang);
                result = new MessageVo(message, filter);
                try {
                    result.setRepoPath(messageService.getMessageFolderRepoPath(message));
                    result.setBookmarked(messageService.getBookmarks().contains(id));
                    List<RepoFileVo> attachments = messageService.getMessageAttacthments(message);
                    if (attachments.size() > 0) {
                        result.setAttachments(attachments);
                    }
                } catch (IOException e) {
                    log.warn("Failed looking up repo-path for message " + messageId, e);
                }
            }
        }

        return result;
    }

    /**
     * Saves a new message
     *
     * @param message the message to save
     * @return the persisted message
     */
    @POST
    @Path("/message")
    @Consumes("application/json")
    @Produces("application/json")
    @GZIP
    @NoCache
    @RolesAllowed({"editor"})
    public MessageVo createMessage(MessageVo message) throws Exception {
        log.info("Creating message " + message);
        Message msg = messageService.createMessage(message);
        return getMessage(msg.getId().toString(), null);
    }

    /**
     * Updates a message
     *
     * @param message the message to update
     * @return the updated message
     */
    @PUT
    @Path("/message")
    @Consumes("application/json")
    @Produces("application/json")
    @GZIP
    @NoCache
    @RolesAllowed({"editor"})
    public MessageVo updateMessage(MessageVo message) throws Exception {
        log.info("Updating message " + message);
        Message msg = messageService.updateMessage(message);
        return getMessage(msg.getId().toString(), null);
    }

    /**
     * Updates the status of a message
     *
     * @param status the status update
     * @return the updated message
     */
    @PUT
    @Path("/update-status")
    @Consumes("application/json")
    @Produces("application/json")
    @GZIP
    @NoCache
    @RolesAllowed({"editor"})
    public MessageVo updateMessageStatus(MessageStatusVo status) throws Exception {
        log.info("Updating status of message " + status.getMessageId() + " to " + status.getStatus());
        Message msg = messageService.setStatus(status.getMessageId(), status.getStatus());
        return getMessage(msg.getId().toString(), null);
    }

    /***************************
     * Template functionality
     ***************************/

    /**
     * Transforms a message according to the requested template and language
     *
     * @param transformVo the transformation data
     * @return the result
     */
    @POST
    @Path("/transform")
    @Consumes("application/json")
    @Produces("text/plain;charset=UTF-8")
    @GZIP
    @NoCache
    @RolesAllowed({"editor"})
    public String transform(TransformVo transformVo) throws Exception {
        log.info("Transforming message " + transformVo);

        return messageService.transformMessage(transformVo.getMessage(), transformVo.getTemplate(), transformVo.getLang());
    }


    /***************************************/
    /** Message History methods           **/
    /***************************************/

    /**
     * Returns the message history for the given message ID
     * @param messageId the message ID or message series ID
     * @return the message history
     */
    @GET
    @Path("/history/{messageId}")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    @RolesAllowed({"editor"})
    public List<MessageHistoryVo> getMessageHistory(@PathParam("messageId") String messageId) {

        // Get the message id
        Integer id = getMessageId(messageId);

        return messageService.getMessageHistory(id);
    }


    /***************************************/
    /** Bookmark methods                  **/
    /***************************************/

    /**
     * Adds a bookmark for the calling user
     *
     * @param messageId the id of the message
     * @return if the bookmark was added
     */
    @POST
    @Path("/bookmark/{messageId}")
    @NoCache
    @RolesAllowed({"user"})
    public boolean addBookmark(@PathParam("messageId") String messageId) {

        // Get the message id
        Integer id = getMessageId(messageId);

        return messageService.addBookmark(id);
    }

    /**
     * Removes a bookmark for the calling user
     *
     * @param messageId the id of the message
     * @return if the bookmark was removed
     */
    @DELETE
    @Path("/bookmark/{messageId}")
    @NoCache
    @RolesAllowed({"user"})
    public boolean removeBookmark(@PathParam("messageId") String messageId) {

        // Get the message id
        Integer id = getMessageId(messageId);

        return messageService.removeBookmark(id);
    }

    /***************************
     * Search functionality
     ***************************/

    /**
     * Main search method
     */
    @GET
    @Path("/search")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    public MessageSearchResult search(
            @QueryParam("lang") String language,
            @QueryParam("q") String query,
            @QueryParam("status") @DefaultValue("PUBLISHED") String status,
            @QueryParam("type") String type,
            @QueryParam("loc") String loc,
            @QueryParam("areas") String areas,
            @QueryParam("categories") String categories,
            @QueryParam("charts") String charts,
            @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate,
            @QueryParam("maxHits") @DefaultValue("100") int maxHits,
            @QueryParam("startIndex") @DefaultValue("0") int startIndex,
            @QueryParam("sortBy") @DefaultValue("DATE") String sortBy,
            @QueryParam("sortOrder") @DefaultValue("DESC") String sortOrder,
            @QueryParam("mapMode") @DefaultValue("false") boolean mapMode
    ) throws Exception {
        long t0 = System.currentTimeMillis();
        MessageSearchParams params = MessageSearchParams.readParams(language, query, status, type, loc, areas, categories, charts, fromDate, toDate, maxHits, startIndex, sortBy, sortOrder, mapMode);
        MessageSearchResult searchResult = messageSearchService.search(params);
        log.info(String.format("Search [%s] returns %d of %d messages in %d ms", params.toString(), searchResult.getMessages().size(), searchResult.getTotal(), System.currentTimeMillis() - t0));
        return searchResult;
    }

    /**
     * Published messages method
     */
    @GET
    @Path("/published")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    public MessageSearchResult searchPublished(
            @QueryParam("lang") String language,
            @QueryParam("sortBy") @DefaultValue("DATE") String sortBy,
            @QueryParam("sortOrder") @DefaultValue("DESC") String sortOrder,
            @QueryParam("attachments") @DefaultValue("false") boolean attachments
    ) throws Exception {
        long t0 = System.currentTimeMillis();
        MessageSearchParams params = MessageSearchParams.readParams(language, "", "PUBLISHED", "", "", "", "", "", "", "", 1000, 0, sortBy, sortOrder, false);
        MessageSearchResult searchResult =  messageSearchService.search(params);
        if (attachments) {
            searchResult.getMessages().stream()
                    .forEach(msg -> {
                        try {
                            List<RepoFileVo> repoFiles = messageService.getMessageAttacthments(msg.getId());
                            if (repoFiles.size() > 0) {
                                msg.setAttachments(repoFiles);
                            }
                        } catch (Exception e) {
                            log.warn("Failed loading attachments for " + msg.getId());
                        }
                    });
        }
        log.trace(String.format("Search [%s] returns %d of %d messages in %d ms", params.toString(), searchResult.getMessages().size(), searchResult.getTotal(), System.currentTimeMillis() - t0));
        return searchResult;
    }

    /**
     * Published firing exercises
     */
    @GET
    @Path("/active-firing-exercises")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    public MessageSearchResult searchFiringExercises(
            @QueryParam("lang") String language
    ) throws Exception {
        String categoryId = String.valueOf(categoryService.findOrCreateFiringExercisesCategory().getId());
        MessageSearchParams params = MessageSearchParams.readParams(language, "", "PUBLISHED", "MSI", "", "", categoryId, "", "", "", 1000, 0, "AREA", "ASC", false);
        return messageSearchService.search(params);
    }

    /***************************
     * PDF functionality
     ***************************/

    /**
     * Returns a PDF for the search result
     */
    @GET
    @Path("/search-pdf")
    @Produces("application/pdf")
    @NoCache
    public Response generatePdf(
            @QueryParam("lang") String language,
            @QueryParam("q") String query,
            @QueryParam("status") @DefaultValue("PUBLISHED") String status,
            @QueryParam("type") String type,
            @QueryParam("loc") String loc,
            @QueryParam("areas") String areas,
            @QueryParam("categories") String categories,
            @QueryParam("charts") String charts,
            @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate,
            @QueryParam("sortBy") @DefaultValue("DATE") String sortBy,
            @QueryParam("sortOrder") @DefaultValue("DESC") String sortOrder,
            @QueryParam("mapMode") @DefaultValue("false") boolean mapMode
    ) throws Exception {
        MessageSearchParams params = MessageSearchParams.readParams(language, query, status, type, loc, areas, categories, charts, fromDate, toDate, 1000, 0, sortBy, sortOrder, mapMode);
        MessageSearchResult result = messageSearchService.search(params);

        String template = "message-list.ftl";
        String bundle = "MessageList";
        Map<String, Object> data = new HashMap<>();
        data.put("messages", result.getMessages());
        data.put("areaHeadings", "AREA".equals(sortBy));

        try {
            StreamingOutput stream = os -> {
                try {
                    pdfService.generatePdf(data, template, language, bundle, os);
                } catch (Exception e) {
                    throw new WebApplicationException("Error generating PDF", e);
                }
            };

            return Response
                    .ok(stream)
                    .type("application/pdf")
                    .header("Content-Disposition", "attachment; filename=\"messages.pdf\"")
                    .build();

        } catch (Exception e) {
            log.error("error generating PDF from template " + template, e);
            throw e;
        }
    }

    /**
     * Returns a PDF for the search result
     */
    @GET
    @Path("/message-pdf/{messageId}")
    @Produces("application/pdf")
    @NoCache
    public Response generatePdf(@PathParam("messageId") String messageId, @QueryParam("lang") String lang) {
        // Strip any ".pdf" suffix
        if (messageId.toLowerCase().endsWith(".pdf")) {
            messageId = messageId.substring(0, messageId.length() - 4);
        }

        // Get the message
        MessageVo message = getMessage(messageId, lang);

        String template = "message-details.ftl";
        String bundle = "MessageList";
        Map<String, Object> data = new HashMap<>();
        data.put("msg", message);

        try {
            StreamingOutput stream = os -> {
                try {
                    pdfService.generatePdf(data, template, lang, bundle, os);
                } catch (Exception e) {
                    throw new WebApplicationException("Error generating PDF", e);
                }
            };

            return Response
                    .ok(stream)
                    .type("application/pdf")
                    .header("Content-Disposition", "attachment; filename=\"" + message.getSeriesIdentifier().getFullId() + ".pdf\"")
                    .build();

        } catch (Exception e) {
            log.error("error generating PDF from template " + template, e);
            throw e;
        }
    }

    /***************************
     * Calendar functionality
     ***************************/

    /**
     * Returns an iCalendar ICS file for the message
     */
    @GET
    @Path("/message-cal/{messageId}")
    @Produces(TYPE_ICALENDAR)
    @NoCache
    public Response generateCalendar(@PathParam("messageId") String messageId, @QueryParam("lang") String lang) {
        // Strip any ".ics" suffix
        if (messageId.toLowerCase().endsWith(".ics")) {
            messageId = messageId.substring(0, messageId.length() - 4);
        }

        // Get the message
        MessageVo message = getMessage(messageId, lang);

        // Generate the calendar data
        try {
            StreamingOutput stream = os -> {
                try {
                    List<MessageVo> messages = new ArrayList<>();
                    messages.add(message);
                    calendarService.generateCalendarData(messages, app.getLanguage(lang), os);
                } catch (Exception e) {
                    throw new WebApplicationException("Error generating calendar data", e);
                }
            };

            return Response
                    .ok(stream)
                    .type(TYPE_ICALENDAR)
                    .header("Content-Disposition", "attachment; filename=\"" + message.getSeriesIdentifier().getFullId() + ".ics\"")
                    .build();

        } catch (Exception e) {
            log.error("error generating calendar for message " + messageId, e);
            throw e;
        }
    }

    /**
     * Returns an iCalendar ICS file for the message
     */
    @GET
    @Path("/active-msinm.ics")
    @Produces(TYPE_ICALENDAR)
    @NoCache
    public Response activeMsiNmCalendar(final @QueryParam("lang") String lang) {

        MessageSearchParams params = new MessageSearchParams();
        params.setLanguage(app.getLanguage(lang));
        params.setStartIndex(0);
        params.setMaxHits(1000);
        params.setSortBy(MessageSearchParams.SortBy.DATE);
        params.setSortOrder(MessageSearchParams.SortOrder.DESC);
        params.setStatus(Status.PUBLISHED);

        MessageSearchResult result = messageSearchService.search(params);

        // Generate the calendar data
        try {
            StreamingOutput stream = os -> {
                try {
                    calendarService.generateCalendarData(result.getMessages(), app.getLanguage(lang), os);
                } catch (Exception e) {
                    throw new WebApplicationException("Error generating calendar data", e);
                }
            };

            return Response
                    .ok(stream)
                    .type(TYPE_ICALENDAR)
                    .header("Content-Disposition", "attachment; filename=\"active_msi_nm.ics\"")
                    .build();

        } catch (Exception e) {
            log.error("error generating calendar for active MSI-NM messages", e);
            throw e;
        }
    }


    /**
     * Re-creates the message search index.
     * Requires the "admin" role
     */
    @GET
    @Path("/recreate-search-index")
    @RolesAllowed({"admin"})
    public String recreateSearchIndex() {
        try {
            log.info("Recreating message search index");
            messageSearchService.recreateIndex();
        } catch (IOException e) {
            log.error("Error recreating message search index");
        }
        return "OK";
    }

    /***************************
     * Publisher functionality
     ***************************/

    /**
     * Returns the list of available publishers
     * @return the list of available publishers
     */
    @GET
    @Path("/publishers")
    @RolesAllowed({"admin"})
    @GZIP
    @NoCache
    public List<PublisherContext> getPublishers() {
        return publishingService.getPublisherContexts();
    }

    /**
     * Updates a publishers active status
     *
     * @param publisher the publisher to update
     * @return the updated publisher
     */
    @PUT
    @Path("/publisher")
    @Consumes("application/json")
    @Produces("application/json")
    @GZIP
    @NoCache
    @RolesAllowed({"admin"})
    public PublisherContext updatePublisher(PublisherContext publisher) throws Exception {
        log.info("Setting active status of publisher " + publisher.getType() + " to " + publisher.isActive());
        return publishingService.updatePublisherContext(publisher);
    }


    /***************************
     * Time parsing functionality
     ***************************/

    /**
     * Translates the time description and determines validFrom and validTo from it
     *
     * @param timeVo the time to translate
     * @return the translated time
     */
    @POST
    @Path("/translate-time")
    @Consumes("application/json")
    @Produces("application/json")
    @GZIP
    @NoCache
    public MessageTimeVo translateTime(MessageTimeVo timeVo) {
        if (timeVo.getTimes() != null && timeVo.getTimes().size() > 0 &&
                StringUtils.isNotBlank(timeVo.getTimes().get(0).getTime())) {
            LocalizedTimeVo srcTime = timeVo.getTimes().get(0);
            try {
                String timeEn = ("en".equals(srcTime.getLang()))
                        ? srcTime.getTime()
                        : TimeTranslator.get(srcTime.getLang()).translateToEnglish(srcTime.getTime());
                for (int x = 1; x < timeVo.getTimes().size(); x++) {
                    LocalizedTimeVo vo = timeVo.getTimes().get(x);
                    if ("en".equals(vo.getLang())) {
                        vo.setTime(timeEn);
                    } else {
                        vo.setTime(TimeTranslator.get(vo.getLang()).translateFromEnglish(timeEn));
                    }
                }

                // Compute validFrom and validTo...
                TimeModel model = TimeParser.get().parseModel(timeEn);
                Date[] dates = TimeProcessor.getDateInterval(model);
                timeVo.setValidFrom(dates[0]);
                timeVo.setValidTo(dates[1]);
            } catch (Exception e) {
                log.warn("Failed translating time " + timeVo + ": " + e);
            }
        }

        // No result
        return timeVo;
    }

    /***************************
     * Helper VO classes
     ***************************/

    /**
     * Helper class used changing the status of a message
     */
    public static class MessageStatusVo implements JsonSerializable {
        Integer messageId;
        Status status;

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
    }

    /**
     * Helper class used for submitting message transformation data
     */
    public static class TransformVo implements JsonSerializable {
        MessageVo message;
        String template;
        String lang;

        public MessageVo getMessage() {
            return message;
        }

        public void setMessage(MessageVo message) {
            this.message = message;
        }

        public String getTemplate() {
            return template;
        }

        public void setTemplate(String template) {
            this.template = template;
        }

        public String getLang() {
            return lang;
        }

        public void setLang(String lang) {
            this.lang = lang;
        }
    }

    /**
     * Helper class used for translating time descriptions
     */
    public static class MessageTimeVo implements JsonSerializable {
        Date validFrom, validTo;
        List<LocalizedTimeVo> times;

        public Date getValidFrom() {
            return validFrom;
        }

        public void setValidFrom(Date validFrom) {
            this.validFrom = validFrom;
        }

        public Date getValidTo() {
            return validTo;
        }

        public void setValidTo(Date validTo) {
            this.validTo = validTo;
        }

        public List<LocalizedTimeVo> getTimes() {
            return times;
        }

        public void setTimes(List<LocalizedTimeVo> times) {
            this.times = times;
        }
    }

    /**
     * Helper class that contains a localized time description
     */
    public static class LocalizedTimeVo {
        String lang, time;

        public String getLang() {
            return lang;
        }

        public void setLang(String lang) {
            this.lang = lang;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }

}