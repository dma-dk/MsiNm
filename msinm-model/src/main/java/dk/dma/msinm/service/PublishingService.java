package dk.dma.msinm.service;

import dk.dma.msinm.common.config.CdiHelper;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.vo.MessageVo;
import org.slf4j.Logger;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static dk.dma.msinm.service.Publisher.PublisherContext;

/**
 * Manages the list of Publishers, such as MailPublisher, NavtextPublisher, etc, and
 * serves as an interface between the Messages the the publishers.
 */
@Singleton
@Startup
@Lock(LockType.READ)
public class PublishingService {

    @Inject
    Logger log;

    Map<String, PublisherContext> publisherMap = new ConcurrentHashMap<>();

    /**
     * Registers the publisher
     * @param publisher the publisher to register
     */
    public void registerPublisher(PublisherContext publisher) {
        publisherMap.put(publisher.getType(), publisher);
        log.info("Registered publisher " + publisher.getType());
    }

    /**
     * Updates the new message template with a publication of the registered publishers
     *
     * @param messageVo the new message template
     */
    public void newTemplateMessage(MessageVo messageVo) {
        instantiatePublishers(true).forEach(publisher -> publisher.newTemplateMessage(messageVo));
    }

    /**
     * Prior to creating a new message, let the registered publishers check up on publications.
     * @param message the message about to be created
     */
    public void createMessage(Message message) {
        // Let publishers check up on their own publications
        instantiatePublishers(false).forEach(publisher -> publisher.createMessage(message));
    }

    /**
     * Prior to updating an existing message, let the registered publishers check up on publications.
     * @param message the message about to be updated
     */
    public void updateMessage(Message message) {
        // Let publishers check up on their own publications
        instantiatePublishers(false).forEach(publisher -> publisher.updateMessage(message));
    }

    /**
     * Prior to changing state of an existing message, let the registered publishers check up on publications.
     * @param message the message about to be updated
     */
    public void setStatus(Message message) {
        // Let publishers check up on their own publications
        instantiatePublishers(false).forEach(publisher -> publisher.setStatus(message));
    }

    /**
     * Returns the list of available publishers
     * @return the list of available publishers
     */
    public List<PublisherContext> getPublisherContexts() {
        return publisherMap.values().stream()
                .sorted((p1, p2) -> p2.getPriority() - p1.getPriority())
                .collect(Collectors.toList());
    }

    /**
     * Updates the active status of a publisher
     * @return the active status of a publisher
     */
    public PublisherContext updatePublisherContext(PublisherContext publisherContext) throws Exception {
        Publisher publisher = instantiatePublisher(publisherContext.getType());
        publisher.setActive(publisherContext.isActive());
        return publisherContext;
    }

    /**
     * Returns the list of instantiated publishers sorted by priority
     * @param onlyActive whether to include all, or only active publishers
     * @return the list of publishers
     */
    private List<Publisher> instantiatePublishers(boolean onlyActive) {
        List<Publisher> publishers = new ArrayList<>();
        publisherMap.values().stream()
            .filter(publisher -> !onlyActive || publisher.isActive())
            .sorted((p1, p2) -> p2.getPriority() - p1.getPriority())
            .forEach(ctx -> {
                try {
                    publishers.add(instantiatePublisher(ctx.getPublisherClass()));
                } catch (Exception e) {
                    log.warn("Could not instantiate publisher " + ctx.getType());
                }
            });
        return publishers;
    }

    /**
     * Instantiates the publisher bean for the publication type
     * @param type the publication type
     */
    private Publisher instantiatePublisher(String type) throws NamingException {
        return CdiHelper.getBean(publisherMap.get(type).getPublisherClass());
    }

    /**
     * Instantiates the publisher bean for the given class
     * @param publisherClass the publisher class
     */
    private <T extends Publisher> T instantiatePublisher(Class<T> publisherClass) throws NamingException {
        return CdiHelper.getBean(publisherClass);
    }

}
