package dk.dma.msinm.common.mail;

import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;

/**
 * Interface for sending emails
 */
@Singleton
@Startup
public class MailService {

    @Resource(name = "java:jboss/mail/MsiNm")
    Session mailSession;

    @Inject
    Logger log;

    @Inject
    MailAttachmentCache mailAttachmentCache;

    @PostConstruct
    public void start() {
    }

    @PreDestroy
    public void destroy(){
    }

    /**
     * TEST using:
     *  mailService.mailify("http://localhost:8080/mail/user-activation.jsp");
     *  mailService.mailify("http://www.dmi.dk/vejr/til-lands/regionaludsigten/kbhnsjaelland/");
     * @param url the url to mailify
     */
    public void mailify(String url) {
        try {
            HtmlMail doc = HtmlMail.fromUrl(url, true);
            Mail mail = doc.getMail(false)
                    .addFrom(new InternetAddress("peder@carolus.dk"))
                    .addRecipient(Message.RecipientType.TO,	new InternetAddress("peder@carolus.dk"))
                    .doSetSubject("Hello Peder");

            sendMail(mail);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends the given mail synchronously
     * @param mail the mail to send
     */
    public void sendMail(Mail mail) throws MessagingException {
        try {
            log.info("Composing mail for " + mail.getFrom());
            Message message = mail.compose(mailSession, mailAttachmentCache.getCache());
            log.info("Sending...");
            Transport.send(message);
            log.info("Done");

        } catch (MessagingException e) {
            log.error("Failed sending mail for " + mail.getFrom(), e);
            // For now... throw e;
        }
    }

}
