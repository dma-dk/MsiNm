package dk.dma.msinm.service;

import dk.dma.msinm.model.Message;
import dk.dma.msinm.model.Status;
import org.slf4j.Logger;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.util.List;

/**
 * This service sets up a time to regularly check for published messages that have
 * passed their validTo date, and set the status of these to expired.
 */
@Singleton
@Startup
public class MessageExpiryService {

    @Inject
    private Logger log;

    @Inject
    MessageService messageService;

    /**
     * Called every hour to update the status of expired published messages
     */
    @Schedule(persistent = false, second = "27", minute = "48", hour = "*", dayOfWeek = "*", year = "*")
    public void checkForExpiredMessages() {

        List<Message> messages = messageService.findPublishedExpiredMessages();
        if (messages.size() == 0) {
            return;
        }

        log.info("Found " + messages.size() + " expired published messages");
        messages.forEach(msg -> {
            try {
                messageService.setStatus(msg, Status.EXPIRED);
            } catch (Exception e) {
                log.error("Failed setting expired state of message " + msg.getId(), e);
            }
        });
    }
}
