package dk.dma.msinm.publish;

import org.slf4j.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

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

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMessage(Message message) {

        try {
            MapMessage msg = (MapMessage) message;

            log.info("Received JMS message update for ID: " + msg.getInt("ID") + ", status: " + msg.getString("STATUS"));
        } catch (Throwable e) {
            log.error("Failed processing JMS message " + message, e);
        }

    }
}
