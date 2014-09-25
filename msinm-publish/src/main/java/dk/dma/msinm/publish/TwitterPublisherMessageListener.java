package dk.dma.msinm.publish;

import dk.dma.msinm.common.MsiNmApp;
import dk.dma.msinm.common.util.JsonUtils;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.model.Publication;
import dk.dma.msinm.model.Status;
import dk.dma.msinm.service.MessageService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import twitter4j.StatusUpdate;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.MapMessage;
import javax.jms.MessageListener;
import java.net.URL;

import static dk.dma.msinm.publish.TwitterPublisher.TwitterData;

/**
 * Used for listening for message status updates via JMS
 */
@MessageDriven(
        name = "TwitterPublisherMDB",
        activationConfig = {
                @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
                @ActivationConfigProperty(propertyName = "destination", propertyValue = "java:/jms/topic/messageTopic"),
                @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")
        })
public class TwitterPublisherMessageListener implements MessageListener {

    @Inject
    Logger log;

    @Inject
    TwitterPublisher twitterPublisher;

    @Inject
    TwitterProvider twitterProvider;

    @Inject
    MessageService messageService;

    @Inject
    MsiNmApp app;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMessage(javax.jms.Message message) {

        // Check that the Twitter publisher is active
        if (!twitterPublisher.isActive()) {
            return;
        }

        try {
            MapMessage msg = (MapMessage) message;

            // Check if this is a published message
            if (Status.PUBLISHED.name().equals(msg.getString("STATUS"))) {
                log.info("Received JMS message update for ID: " + msg.getInt("ID") + ", status: " + msg.getString("STATUS"));
                publishMessage(msg.getInt("ID"));
            }
        } catch (Throwable e) {
            log.error("Failed processing JMS message " + message, e);
        }
    }

    /**
     * Publish the message to twitter
     * @param id the id of the message
     */
    private void publishMessage(Integer id) throws Exception {
        Message message = messageService.getCachedMessage(id);

        // Check that we should publish Twitter for the message
        Publication publication = message.getPublication(TwitterPublisher.TWITTER_PUBLISHER_TYPE);
        if (publication == null || publication.getData() == null || !publication.isPublish()) {
            return;
        }

        TwitterData twitterData = JsonUtils.fromJson(publication.getData(), TwitterData.class);
        String twitterMessage = twitterData.getMessage();
        if (StringUtils.isBlank(twitterMessage)) {
            return;
        }

        //Instantiate and initialize a new twitter status update
        StatusUpdate statusUpdate = new StatusUpdate(StringUtils.abbreviate(twitterMessage, TwitterProvider.MAX_LENGTH));

        // Attach image
        statusUpdate.setMedia(
                message.getSeriesIdentifier().getFullId(),
                new URL(app.getBaseUri() + "/message-map-image/" + id).openStream());

        //tweet or update status
        // TODO: Robustness and async support
        log.info("Publishing Twitter message: " + twitterMessage);
        twitterProvider.getInstance().updateStatus(statusUpdate);
    }
}
