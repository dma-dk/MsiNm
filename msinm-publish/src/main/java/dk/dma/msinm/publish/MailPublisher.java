package dk.dma.msinm.publish;

import dk.dma.msinm.model.MailList;
import dk.dma.msinm.model.MailListTemplate;
import dk.dma.msinm.model.Status;
import dk.dma.msinm.service.MessageSearchParams;
import dk.dma.msinm.vo.MessageVo;
import dk.dma.msinm.vo.PublicationVo;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

/**
 * Defines a standard mail publisher that handles publishing of messages via e-mail.
 */
@Singleton
@Startup
@Lock(LockType.READ)
public class MailPublisher extends BaseMailPublisher {

    public static final String MAIL_PUBLISHER_TYPE = "mail";

    public static final String TEMPLATE_MESSAGE_UPDATES = "Message Update";
    public static final String TEMPLATE_MESSAGE_UPDATE_LIST = "Message Updates";
    public static final String MAIL_LIST_NAME = "Updated Messages Digest";

    @Inject
    Logger log;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return MAIL_PUBLISHER_TYPE;
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
    public void checkCreateMailListTemplates() {

        try {
            // First check that the template exists
            MailListTemplate listTemplate = mailListService.findTemplateByName(TEMPLATE_MESSAGE_UPDATE_LIST);
            if (listTemplate == null) {
                listTemplate = new MailListTemplate();
                listTemplate.setName(TEMPLATE_MESSAGE_UPDATE_LIST);
                listTemplate.setType(MAIL_PUBLISHER_TYPE);
                listTemplate.setTemplate("maillist-message-list.ftl");
                listTemplate.setBundle("MailListMessageDetails");
                listTemplate.setCollated(true);
                listTemplate = mailListService.saveMailListTemplate(listTemplate);
            }
            MailListTemplate detailTemplate = mailListService.findTemplateByName(TEMPLATE_MESSAGE_UPDATES);
            if (detailTemplate == null) {
                detailTemplate = new MailListTemplate();
                detailTemplate.setName(TEMPLATE_MESSAGE_UPDATES);
                detailTemplate.setType(MAIL_PUBLISHER_TYPE);
                detailTemplate.setTemplate("maillist-message-details.ftl");
                detailTemplate.setBundle("MailListMessageDetails");
                detailTemplate.setCollated(false);
                mailListService.saveMailListTemplate(detailTemplate);
            }

            // Check that the mail list exists
            MailList mailList = mailListService.findPublicMailListByTemplateAndName(listTemplate, MAIL_LIST_NAME);
            if (mailList == null) {
                mailList = new MailList();
                mailList.setName(MAIL_LIST_NAME);
                mailList.setTemplate(listTemplate);
                mailList.setChangedMessages(true);
                mailList.setSchedule(MailList.Schedule.CONTINUOUS);
                mailList.setSendIfEmpty(false);
                mailList.setPublicMailingList(true);

                MessageSearchParams filter = new MessageSearchParams();
                filter.setStatus(Status.PUBLISHED);
                mailListService.updateFilter(mailList, filter, "en");
                mailListService.createMailList(mailList);
            }
        } catch (Exception e) {
            log.error("Failed creating mail list " + e, e);
        }
    }

    /**
     * Called periodically to process the mailing list
     */
    @Schedule(persistent = false, second = "44", minute = "*/5", hour = "*", dayOfWeek = "*", year = "*")
    public void periodicProcessPendingMailingLists() {
        processPendingMailLists();
    }

}
