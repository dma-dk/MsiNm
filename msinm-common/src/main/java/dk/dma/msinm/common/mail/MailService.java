package dk.dma.msinm.common.mail;

import dk.dma.msinm.common.settings.annotation.Setting;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import java.io.StringWriter;
import java.util.Map;

/**
 * Interface for sending emails
 */
@Stateless
public class MailService {

    @Resource(name = "java:jboss/mail/MsiNm")
    Session mailSession;

    @Inject
    @Setting(value = "mailBaseUri", defaultValue = "http://localhost:8080")
    String baseUri;

    @Inject
    @Setting(value = "mailSender", defaultValue = "peder@carolus.dk")
    String mailSender;

    @Inject
    @Setting(value = "mailValidRecipients", defaultValue = "peder@carolus.dk")
    String validRecipients;

    @Inject
    Logger log;

    @Inject
    MailAttachmentCache mailAttachmentCache;

    @Inject
    Configuration mailTemplateConfiguration;

    @PostConstruct
    public void start() {
    }

    @PreDestroy
    public void destroy(){
    }

    /**
     * Sends an email based on a Freemarker template
     * @param template the template
     * @param data the email data
     * @param title the title of the email
     * @param recipients the list of recipients
     */
    public void sendMail(String template, Map<String, Object> data, String title, String... recipients) throws Exception {
        Template fmTemplate;
        try {
            fmTemplate = mailTemplateConfiguration.getTemplate(template);

            // Standard data properties
            data.put("baseUri", baseUri);

            StringWriter html = new StringWriter();
            fmTemplate.process(data, html);

            Mail mail = HtmlMail.fromHtml(html.toString(), baseUri, true, true)
                    .doSetSender(new InternetAddress(mailSender))
                    .addFrom(new InternetAddress(mailSender))
                    .doSetSubject(title);

            ValidMailRecipients mailRecipientFilter = new ValidMailRecipients(validRecipients);
            for (String recipient : recipients) {
                mail.addRecipient(Message.RecipientType.TO,	mailRecipientFilter.filter(recipient));
            }

            sendMail(mail);


        } catch (Exception e) {
            log.error("error sending email from template " + template, e);
            throw e;
        }
    }

    /**
     * Sends the given mail synchronously
     * @param mail the mail to send
     */
    public void sendMail(Mail mail) throws MessagingException {
        try {
            log.info("Composing mail for " + mail.getRecipients());
            Message message = mail.compose(mailSession, mailAttachmentCache.getCache());
            log.info("Sending...");
            Transport.send(message);
            log.info("Done");

        } catch (MessagingException e) {
            log.error("Failed sending mail for " + mail.getFrom(), e);
            throw e;
        }
    }

}
