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
package dk.dma.msinm.common.mail;

import dk.dma.msinm.common.settings.annotation.Setting;
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

/**
 * Interface for sending emails
 */
@Stateless
public class MailService {

    @Resource(name = "java:jboss/mail/MsiNm")
    Session mailSession;

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

    @PostConstruct
    public void start() {
    }

    @PreDestroy
    public void destroy(){
    }

    /**
     * Sends an email
     * @param content the HTML content
     * @param title the title of the email
     * @param baseUri the base URI
     * @param recipients the list of recipients
     */
    public void sendMail(String content, String title, String baseUri, String... recipients) throws Exception {
        try {

            Mail mail = HtmlMail.fromHtml(content, baseUri, true, true)
                    .doSetSender(new InternetAddress(mailSender))
                    .addFrom(new InternetAddress(mailSender))
                    .doSetSubject(title);

            ValidMailRecipients mailRecipientFilter = new ValidMailRecipients(validRecipients);
            for (String recipient : recipients) {
                mail.addRecipient(Message.RecipientType.TO,	mailRecipientFilter.filter(recipient));
            }

            sendMail(mail);


        } catch (Exception e) {
            log.error("error sending email: " + title, e);
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
