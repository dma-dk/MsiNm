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

import dk.dma.msinm.legacy.service.LegacyMsiImportService;
import dk.dma.msinm.service.MessageService;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * REST interface for accessing MSI-NM messages
 */
@Path("/message")
@Stateless
public class MessageRestService {

    @Inject
    private Logger log;

    @EJB
    private MessageService messageService;

    @EJB
    private LegacyMsiImportService legacyMsiImportService;

    public MessageRestService() {
    }

    @GET
    @Path("/import-legacy-msi")
    public String importLegacyMsiWarnings() {
        log.info("Importing legacy MSI warnings");

        return String.format("Created or updated %d legacy MSI warnings",
                legacyMsiImportService.importWarnings());
    }

    @GET
    @Path("/all")
    @Produces("application/json")
    @GZIP
    @NoCache
    public JsonArray getAll() {

        JsonArrayBuilder result = Json.createArrayBuilder();
        messageService.getAll().forEach(msg -> result.add(msg.toJson()));
        return result.build();
    }

}