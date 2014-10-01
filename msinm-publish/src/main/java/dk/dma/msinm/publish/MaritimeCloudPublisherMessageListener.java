package dk.dma.msinm.publish;

import org.slf4j.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.MapMessage;
import javax.jms.MessageListener;


/**
 * Used for listening for message status updates via JMS
 */
@MessageDriven(
        name = "MaritimeCloudPublisherMDB",
        activationConfig = {
                @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
                @ActivationConfigProperty(propertyName = "destination", propertyValue = "java:/jms/topic/messageTopic"),
                @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")
        })
public class MaritimeCloudPublisherMessageListener implements MessageListener {

    @Inject
    Logger log;

    @Inject
    MaritimeCloudPublisher maritimeCloudPublisher;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMessage(javax.jms.Message message) {

        // Check that the maritime cloud service is connected
        if (!maritimeCloudPublisher.isActive() || !maritimeCloudPublisher.connected()) {
            return;
        }

        try {
            MapMessage msg = (MapMessage) message;

            // Check if this is a published message
            //if (Status.PUBLISHED.name().equals(msg.getString("STATUS"))) {
                log.info("Received JMS message update for ID: " + msg.getInt("ID") + ", status: " + msg.getString("STATUS"));
            maritimeCloudPublisher.publishMessage(msg.getInt("ID"));
            //}
        } catch (Throwable e) {
            log.error("Failed processing JMS message " + message, e);
        }
    }
}
