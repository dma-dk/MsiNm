package dk.dma.msinm.common.mail;

import org.slf4j.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

/**
 * Used for sending mails asynchronously via JMS
 */
@MessageDriven(
        name = "mailQueue",
        activationConfig = {
                @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
                @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/mail"),
                @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")
        })
public class MailQueue implements MessageListener {

    @Inject
    Logger log;

    @Inject
    MailService mailService;

    /**
     * Called when a new mail is received in the mail queue
     * @param message the serialize mail
     */
    public void onMessage(Message message) {
        ObjectMessage msg;

        try {
            if (message instanceof ObjectMessage) {
                msg = (ObjectMessage) message;
                Mail mail = (Mail)msg.getObject();
                log.info("Received JMS mail: " + mail);
                mailService.sendMail(mail);
            } else {
                log.warn("Message of wrong type: " + message.getClass().getName());
            }
        } catch (Throwable e) {
            log.error("Failed sending JMS mail received from queue. " + message, e);
        }
    }
}
