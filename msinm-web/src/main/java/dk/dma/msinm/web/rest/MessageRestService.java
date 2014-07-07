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

import dk.dma.msinm.model.Location;
import dk.dma.msinm.model.Status;
import dk.dma.msinm.model.Type;
import dk.dma.msinm.service.MessageSearchParams;
import dk.dma.msinm.service.MessageSearchResult;
import dk.dma.msinm.service.MessageSearchService;
import dk.dma.msinm.service.MessageService;
import dk.dma.msinm.vo.CopyOp;
import dk.dma.msinm.vo.MessageVo;
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
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST interface for accessing MSI-NM messages
 */
@Path("/message")
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

    public MessageRestService() {
    }


    /**
     * Test method - returns all message
     * @return returns all message
     */
    @GET
    @Path("/all")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    public List<MessageVo> getAll() {
        return messageService.getActive()
                .stream()
                .map(message -> new MessageVo(message, CopyOp.get("details")))
                .collect(Collectors.toList());
    }

    private MessageSearchParams readParams(
            String language,
            String query,
            String status,
            String type,
            String loc,
            String fromDate,
            String toDate,
            int maxHits,
            int startIndex,
            String sortBy,
            String sortOrder
    ) throws ParseException {
        log.info(String.format(
                "Search with lang=%s, q=%s, status=%s, type=%s, loc=%s, from=%s, to=%s, maxHits=%d, startIndex=%d, sortBy=%s, sortOrder=%s",
                language, query, status, type, loc, fromDate, toDate, maxHits, startIndex, sortBy, sortOrder));

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
                params.getTypes().add(Type.valueOf(msgType));
            }
        }

        if (StringUtils.isNotBlank(loc)) {
            params.setLocations(Location.fromJson(loc));
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
            @QueryParam("status") @DefaultValue("ACTIVE") String status,
            @QueryParam("type") String type,
            @QueryParam("loc") String loc,
            @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate,
            @QueryParam("maxHits") @DefaultValue("100") int maxHits,
            @QueryParam("startIndex") @DefaultValue("0") int startIndex,
            @QueryParam("sortBy") @DefaultValue("issueDate") String sortBy,
            @QueryParam("sortOrder") @DefaultValue("desc") String sortOrder
    ) throws Exception {
        MessageSearchParams params = readParams(language, query, status, type, loc, fromDate, toDate, maxHits, startIndex, sortBy, sortOrder);
        return messageSearchService.search(params);
    }

    /**
     * Returns a PDF for the search result
     */
    @GET
    @Path("/pdf")
    @Produces("application/pdf")
    @NoCache
    public Response generatePdf(
            @QueryParam("lang") String language,
            @QueryParam("q") String query,
            @QueryParam("status") @DefaultValue("ACTIVE") String status,
            @QueryParam("type") String type,
            @QueryParam("loc") String loc,
            @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate,
            @QueryParam("sortBy") @DefaultValue("issueDate") String sortBy,
            @QueryParam("sortOrder") @DefaultValue("desc") String sortOrder
    ) throws Exception {
        MessageSearchParams params = readParams(language, query, status, type, loc, fromDate, toDate, Integer.MAX_VALUE, 0, sortBy, sortOrder);
        MessageSearchResult result = messageSearchService.search(params);

        String template = "pdf-test.ftl";
        try {
            // Standard data properties
            Map<String, Object> data = new HashMap<>();
            data.put("messages", result.getMessages());

            StreamingOutput stream = new StreamingOutput() {
                @Override
                public void write(OutputStream os) throws IOException, WebApplicationException {

                    try {
                        pdfService.generatePdf(data, template, os);
                    } catch (Exception e) {
                        throw new WebApplicationException("Error generating PDF", e);
                    }
                }
            };

            return Response
                    .ok(stream)
                    .type("application/pdf")
                    .header("Content-Disposition", "attachment; filename=\"messages.pdf\"")
                    .build();


        } catch (Exception e) {
            log.error("error sending email from template " + template, e);
            throw e;
        }
    }

    /**
     * Main search method
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