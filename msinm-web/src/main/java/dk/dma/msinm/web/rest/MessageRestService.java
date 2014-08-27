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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dk.dma.msinm.common.MsiNmApp;
import dk.dma.msinm.common.model.DataFilter;
import dk.dma.msinm.common.templates.PdfService;
import dk.dma.msinm.common.time.TimeModel;
import dk.dma.msinm.common.time.TimeParser;
import dk.dma.msinm.common.time.TimeProcessor;
import dk.dma.msinm.common.time.TimeTranslator;
import dk.dma.msinm.model.Location;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.model.Reference;
import dk.dma.msinm.model.ReferenceType;
import dk.dma.msinm.model.SeriesIdType;
import dk.dma.msinm.model.SeriesIdentifier;
import dk.dma.msinm.model.Status;
import dk.dma.msinm.model.Type;
import dk.dma.msinm.service.CalendarService;
import dk.dma.msinm.service.MessageSearchParams;
import dk.dma.msinm.service.MessageSearchResult;
import dk.dma.msinm.service.MessageSearchService;
import dk.dma.msinm.service.MessageService;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
    MessageSearchService messageSearchService;

    @Inject
    PdfService pdfService;

    @Inject
    CalendarService calendarService;

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
        MessageVo message = getMessage(messageId, null);

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


    /***************************
     * Search functionality
     ***************************/

    /**
     * Parses the request parameters and collects them in a MessageSearchParams entity
     */
    private MessageSearchParams readParams(
            String language,
            String query,
            String status,
            String type,
            String loc,
            String areas,
            String fromDate,
            String toDate,
            int maxHits,
            int startIndex,
            String sortBy,
            String sortOrder,
            boolean mapMode
    ) throws ParseException {
        log.info(String.format(
                "Search with lang=%s, q=%s, status=%s, type=%s, loc=%s, areas=%s, from=%s, to=%s, maxHits=%d, startIndex=%d, sortBy=%s, sortOrder=%s, mapMode=%b",
                language, query, status, type, loc, areas, fromDate, toDate, maxHits, startIndex, sortBy, sortOrder, mapMode));

        MessageSearchParams params = new MessageSearchParams();
        params.setLanguage(language);
        params.setStartIndex(startIndex);
        params.setMaxHits(maxHits);
        params.setMapMode(mapMode);

        try {
            params.setSortBy(MessageSearchParams.SortBy.valueOf(sortBy));
        } catch (Exception e) {
            log.debug("Failed parsing sortBy parameter " + sortBy);
        }

        try {
            params.setSortOrder(MessageSearchParams.SortOrder.valueOf(sortOrder));
        } catch (Exception e) {
            log.debug("Failed parsing sortOrder parameter " + sortOrder);
        }

        params.setQuery(query);

        if (StringUtils.isNotBlank(status)) {
            params.setStatus(Status.valueOf(status));
        }

        if (StringUtils.isNotBlank(type)) {
            for (String msgType : type.split(",")) {
                if (msgType.equals("MSI") || msgType.equals("NM")) {
                    params.getMainTypes().add(SeriesIdType.valueOf(msgType));
                } else {
                    params.getTypes().add(Type.valueOf(msgType));
                }
            }
        }

        if (StringUtils.isNotBlank(loc)) {
            params.setLocations(Location.fromJson(loc));
        }

        if (StringUtils.isNotBlank(areas)) {
            for (String areaId : areas.split(",")) {
                params.getAreaIds().add(Integer.valueOf(areaId));
            }
        }

        if (StringUtils.isNotBlank(fromDate)) {
            DateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            params.setFrom(sdf.parse(fromDate));
        }

        if (StringUtils.isNotBlank(toDate)) {
            DateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            params.setTo(sdf.parse(toDate));
        }

        return params;
    }

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
            @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate,
            @QueryParam("maxHits") @DefaultValue("100") int maxHits,
            @QueryParam("startIndex") @DefaultValue("0") int startIndex,
            @QueryParam("sortBy") @DefaultValue("DATE") String sortBy,
            @QueryParam("sortOrder") @DefaultValue("DESC½") String sortOrder,
            @QueryParam("mapMode") @DefaultValue("false") boolean mapMode
    ) throws Exception {
        MessageSearchParams params = readParams(language, query, status, type, loc, areas, fromDate, toDate, maxHits, startIndex, sortBy, sortOrder, mapMode);
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
            @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate,
            @QueryParam("sortBy") @DefaultValue("DATE") String sortBy,
            @QueryParam("sortOrder") @DefaultValue("DESC") String sortOrder,
            @QueryParam("mapMode") @DefaultValue("false") boolean mapMode
    ) throws Exception {
        MessageSearchParams params = readParams(language, query, status, type, loc, areas, fromDate, toDate, 1000, 0, sortBy, sortOrder, mapMode);
        MessageSearchResult result = messageSearchService.search(params);

        String template = "message-list.ftl";
        String bundle = "MessageList";
        Map<String, Object> data = new HashMap<>();
        data.put("messages", result.getMessages());

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
    public void recreateSearchIndex() {
        try {
            log.info("Recreating message search index");
            messageSearchService.recreateIndex();
        } catch (IOException e) {
            log.error("Error recreating message search index");
        }
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
    @JsonIgnoreProperties(ignoreUnknown=true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class MessageStatusVo {
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
    @JsonIgnoreProperties(ignoreUnknown=true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class TransformVo {
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
    @JsonIgnoreProperties(ignoreUnknown=true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class MessageTimeVo {
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