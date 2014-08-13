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
import dk.dma.msinm.common.templates.PdfService;
import dk.dma.msinm.model.Location;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.model.SeriesIdType;
import dk.dma.msinm.model.Status;
import dk.dma.msinm.model.Type;
import dk.dma.msinm.service.CalendarService;
import dk.dma.msinm.service.MessageSearchParams;
import dk.dma.msinm.service.MessageSearchResult;
import dk.dma.msinm.service.MessageSearchService;
import dk.dma.msinm.service.MessageService;
import dk.dma.msinm.vo.MessageVo;
import edu.emory.mathcs.backport.java.util.Collections;
import org.apache.commons.lang.StringUtils;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
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
import java.util.HashMap;
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
     * Translates the messageId, which may be an ID or a series identifier, into a message id
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
     * Test method - returns all message
     * @return returns all message
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
            }
        }

        return result;
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

    /**
     * Returns an iCalendar ICS file for the message
     */
    @GET
    @Path("/message-cal/{messageId}")
    @Produces("text/calendar")
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
                    .type("text/calendar")
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
    @Produces("text/calendar")
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
                    .type("text/calendar")
                    .header("Content-Disposition", "attachment; filename=\"active_msi_nm.ics\"")
                    .build();

        } catch (Exception e) {
            log.error("error generating calendar for active MSI-NM messages", e);
            throw e;
        }
    }


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
            String sortOrder
    ) throws ParseException {
        log.info(String.format(
                "Search with lang=%s, q=%s, status=%s, type=%s, loc=%s, areas=%s, from=%s, to=%s, maxHits=%d, startIndex=%d, sortBy=%s, sortOrder=%s",
                language, query, status, type, loc, areas, fromDate, toDate, maxHits, startIndex, sortBy, sortOrder));

        MessageSearchParams params = new MessageSearchParams();
        params.setLanguage(language);
        params.setStartIndex(startIndex);
        params.setMaxHits(maxHits);

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
            @QueryParam("sortBy") @DefaultValue("issueDate") String sortBy,
            @QueryParam("sortOrder") @DefaultValue("desc") String sortOrder
    ) throws Exception {
        MessageSearchParams params = readParams(language, query, status, type, loc, areas, fromDate, toDate, maxHits, startIndex, sortBy, sortOrder);
        return messageSearchService.search(params);
    }

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
            @QueryParam("sortBy") @DefaultValue("issueDate") String sortBy,
            @QueryParam("sortOrder") @DefaultValue("desc") String sortOrder
    ) throws Exception {
        MessageSearchParams params = readParams(language, query, status, type, loc, areas, fromDate, toDate, Integer.MAX_VALUE, 0, sortBy, sortOrder);
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
     * Re-creates the message search index.
     * Requires the "admin" role
     */
    @GET
    @Path("/recreate-search-index")
    @RolesAllowed({ "admin" })
    public void recreateSearchIndex() {
        try {
            log.info("Recreating message search index");
            messageSearchService.recreateIndex();
        } catch (IOException e) {
            log.error("Error recreating message search index");
        }
    }


}