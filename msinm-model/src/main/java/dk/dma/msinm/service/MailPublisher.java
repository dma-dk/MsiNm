package dk.dma.msinm.service;

import dk.dma.msinm.vo.MessageVo;
import dk.dma.msinm.vo.PublicationVo;
import org.slf4j.Logger;

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

    @Inject
    Logger log;

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
}
