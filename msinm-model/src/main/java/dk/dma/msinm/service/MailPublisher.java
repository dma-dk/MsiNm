package dk.dma.msinm.service;

import dk.dma.msinm.model.MailList;
import dk.dma.msinm.model.MailListTemplate;
import dk.dma.msinm.vo.MessageVo;
import dk.dma.msinm.vo.PublicationVo;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

/**
 * Defines a standard mail publisher that handles publishing of messages via e-mail.
 */
@Singleton
@Startup
@Lock(LockType.READ)
public class MailPublisher extends Publisher {

    public static final String MAIL_LIST_TEMPLATE_NAME = "Message Details";
    public static final String MAIL_LIST_NAME = "Updated Messages";

    @Inject
    Logger log;

    @Inject
    MailListService mailListService;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return "mail";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {
        return 200;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void newTemplateMessage(MessageVo messageVo) {
        PublicationVo publication = new PublicationVo();
        publication.setType(getType());
        publication.setPublish(true);
        messageVo.checkCreatePublications().add(publication);
    }

    /**
     * Checks that the system mail list used by the MailPublisher exists.
     * If not, create it
     */
    @PostConstruct
    public void checkCreateMailList() {

        try {
            // First check that the template exists
            MailListTemplate template = mailListService.findTemplateByName(MAIL_LIST_TEMPLATE_NAME);
            if (template == null) {
                template = new MailListTemplate();
                template.setName(MAIL_LIST_TEMPLATE_NAME);
                template.setTemplate("maillist-message-details.ftl");
                template.setBundle("MailListMessageDetails");
                template.setCollated(true);
                template = mailListService.saveMailListTemplate(template);
            }

            // Check that the mail list exists
            MailList mailList = mailListService.findPublicMailListByTemplateAndName(template, MAIL_LIST_NAME);
            if (mailList == null) {
                mailList = new MailList();
                mailList.setName(MAIL_LIST_NAME);
                mailList.setTemplate(template);
                mailList.setChangedMessages(true);
                mailList.setSchedule(MailList.Schedule.CONTINUOUS);
                mailList.setSendIfEmpty(false);
                mailList.setPublicMailingList(true);
                mailListService.createMailList(mailList);
            }
        } catch (Exception e) {
            log.error("Failed creating mail list " + e, e);
        }
    }

}
