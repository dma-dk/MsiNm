package dk.dma.msinm.service;

import dk.dma.msinm.common.service.BaseService;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.model.NavwarnMessage;
import dk.dma.msinm.model.NoticeMessage;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import java.util.List;

/**
 * Business interface for accessing MSI-NM messages
 */
@Stateless
public class MessageService extends BaseService {

    @Inject
    private Logger log;

    public NavwarnMessage create(NavwarnMessage navwarnMessage) {
        log.info("Creating navwarn message");
        return saveEntity(navwarnMessage);
    }

    public NoticeMessage create(NoticeMessage noticeMessage) {
        log.info("Creating notice message");
        return saveEntity(noticeMessage);
    }

    public List<Message> getAll() {
        return getAll(Message.class);
    }

    public Message findById(Integer id) {
        return getByPrimaryKey(Message.class, id);
    }

    public Message findByMessageSeriesId(int messageNumber, int messageYear, String messageAuthority) {
        // Execute and return the result
        try {
            return em
                    .createNamedQuery("Message.findBySeriesIdentifier", Message.class)
                    .setParameter("number", messageNumber)
                    .setParameter("year", messageYear)
                    .setParameter("authority", messageAuthority)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

}
