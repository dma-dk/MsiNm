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
import dk.dma.msinm.model.MessageStatus;
import dk.dma.msinm.model.MessageType;
import dk.dma.msinm.service.MessageSearchParams;
import dk.dma.msinm.service.MessageSearchService;
import dk.dma.msinm.service.MessageService;
import org.apache.commons.lang.StringUtils;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

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
    public JsonArray getAll() {

        JsonArrayBuilder result = Json.createArrayBuilder();
        messageService.getActive().forEach(msg -> result.add(msg.toJson()));
        return result.build();
    }

    /**
     * Main search method
     */
    @GET
    @Path("/search")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    public JsonObject search(
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

        log.info(String.format(
                "Search with q=%s, status=%s, type=%s, loc=%s, from=%s, to=%s, maxHits=%d, startIndex=%d, sortBy=%s, sortOrder=%s",
                query, status, type, loc, fromDate, toDate, maxHits, startIndex, sortBy, sortOrder));

        MessageSearchParams params = new MessageSearchParams();
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
            params.setStatus(MessageStatus.valueOf(status));
        }

        if (StringUtils.isNotBlank(type)) {
            for (String msgType : type.split(",")) {
                params.getTypes().add(MessageType.valueOf(msgType));
            }
        }

        if (StringUtils.isNotBlank(loc)) {
            params.setLocation(Location.fromJson(loc));
        }

        if (StringUtils.isNotBlank(fromDate)) {
            DateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            params.setFrom(sdf.parse(fromDate));
        }

        if (StringUtils.isNotBlank(toDate)) {
            DateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            params.setTo(sdf.parse(toDate));
        }

        return messageSearchService
                .search(params)
                .toJson()
                .build();
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