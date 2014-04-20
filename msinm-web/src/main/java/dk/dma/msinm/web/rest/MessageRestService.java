package dk.dma.msinm.web.rest;

import dk.dma.msinm.model.Message;
import dk.dma.msinm.service.MessageService;
import org.slf4j.Logger;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.List;

/**
 * REST interface for accessing MSI-NM messages
 */
@Path("/message")
public class MessageRestService {

    @Inject
    private Logger log;

    @EJB
    private MessageService messageService;

    public MessageRestService() {
    }

    @GET
    @Path("/test")
    public String test() {
        log.info("Hello from test");
        return "Hello world";
    }

    @GET
    @Path("/all")
    @Produces("application/json")
    public List<Message> getAll() {

        return messageService.getAll();
    }



}