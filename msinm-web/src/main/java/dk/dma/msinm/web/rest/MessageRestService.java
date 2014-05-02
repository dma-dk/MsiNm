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

import dk.dma.msinm.common.audit.Auditor;
import dk.dma.msinm.legacy.service.LegacyMsiImportService;
import dk.dma.msinm.model.MessageLocation;
import dk.dma.msinm.model.MessageStatus;
import dk.dma.msinm.model.MessageType;
import dk.dma.msinm.service.MessageSearchParams;
import dk.dma.msinm.service.MessageSearchService;
import dk.dma.msinm.service.MessageService;
import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.ws.rs.*;

/**
 * REST interface for accessing MSI-NM messages
 */
@Path("/message")
@Stateless
public class MessageRestService {

    @Inject
    Logger log;

    @Inject
    Auditor auditor;

    @Inject
    MessageService messageService;

    @Inject
    MessageSearchService messageSearchService;

    @Inject
    LegacyMsiImportService legacyMsiImportService;

    public MessageRestService() {
    }

    @GET
    @Path("/import-legacy-msi")
    public String importLegacyMsiWarnings() {
        log.info("Importing legacy MSI warnings");

        int result = legacyMsiImportService.importWarnings();
        auditor.info("Created or updated %s legacy MSI warnings", String.valueOf(result));

        return String.format("Created or updated %d legacy MSI warnings", result);
    }

    @GET
    @Path("/all")
    @Produces("application/json")
    @GZIP
    @NoCache
    public JsonArray getAll() {

        JsonArrayBuilder result = Json.createArrayBuilder();
        messageService.getActive().forEach(msg -> result.add(msg.toJson()));
        return result.build();
    }

    @GET
    @Path("/search")
    @Produces("application/json")
    @GZIP
    @NoCache
    public JsonArray search(
            @QueryParam("q") String query,
            @QueryParam("status") @DefaultValue("ACTIVE") String status,
            @QueryParam("type") String type,
            @QueryParam("loc") String loc) {

        log.info(String.format("Search with q=%s, status=%s, type=%s, loc=%s", query, status, type, loc));
        MessageSearchParams params = new MessageSearchParams();

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
            params.setLocation(MessageLocation.fromJson(loc));
        }

        JsonArrayBuilder result = Json.createArrayBuilder();
        messageSearchService
                .search(params)
                .forEach(msg -> result.add(msg.toJson()));
        return result.build();
    }
}