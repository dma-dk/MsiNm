package dk.dma.msinm.web.rest;

import dk.dma.msinm.legacy.LegacyMsiService;
import dk.dma.msinm.service.MessageService;
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
    private LegacyMsiService legacyMsiService;

    public MessageRestService() {
    }

    @GET
    @Path("/import-legacy-msi")
    public String importLegacyMsiWarnings() {
        log.info("Importing legacy MSI warnings");

        return String.format("Created or updated %d legacy MSI warnings",
                legacyMsiService.importWarnings());
    }

    @GET
    @Path("/all")
    @Produces("application/json")
    public JsonArray getAll() {

        JsonArrayBuilder result = Json.createArrayBuilder();
        messageService.getAll().forEach(msg -> result.add(msg.toJson()));
        return result.build();
    }



}