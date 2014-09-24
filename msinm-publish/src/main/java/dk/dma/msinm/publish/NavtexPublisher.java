package dk.dma.msinm.publish;

import dk.dma.msinm.common.settings.annotation.Setting;
import dk.dma.msinm.common.util.JsonUtils;
import dk.dma.msinm.model.MailList;
import dk.dma.msinm.model.MailListTemplate;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.model.Publication;
import dk.dma.msinm.model.SeriesIdType;
import dk.dma.msinm.model.Status;
import dk.dma.msinm.service.MessageSearchParams;
import dk.dma.msinm.service.Publisher;
import dk.dma.msinm.user.User;
import dk.dma.msinm.vo.MessageVo;
import dk.dma.msinm.vo.PublicationVo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Defines a standard NAVTEX publisher that handles publishing of messages via NAVTEX.
 */
@Singleton
@Startup
@Lock(LockType.READ)
public class NavtexPublisher extends BaseMailPublisher {

    public static final String NAVTEX_PUBLISHER_TYPE = "navtex";

    public static final String TEMPLATE_NAVTEX_NAME = "Navtex";
    public static final String MAIL_LIST_NAME_PREFIX = "Navtex for ";

    @Inject
    Logger log;

    @Inject
    @Setting(value = "publishNavtextTransmitters", defaultValue = "BALTICO,ROGALAND")
    String[] transmitters;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return NAVTEX_PUBLISHER_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {
        return 100;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void newTemplateMessage(MessageVo messageVo) {
        PublicationVo publication = new PublicationVo();
        publication.setType(getType());
        publication.setPublish(false);

        // Enable Navtex for MSI messages only
        publication.setMessageTypes(Collections.singleton(SeriesIdType.MSI.name()));

        NavtexData data = new NavtexData();
        for (String transmitter : transmitters) {
            data.getTransmitter().put(transmitter, Boolean.FALSE);
        }
        data.setPriority(NavtexPriority.ROUTINE);
        data.setMessage("");
        try {
            publication.setData(JsonUtils.toJson(data));
        } catch (IOException e) {
            log.warn("Failed formatting publication data " + data);
        }

        messageVo.checkCreatePublications().add(publication);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createMessage(Message message) {
        checkNavtexValid(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateMessage(Message message) {
        checkNavtexValid(message);
    }

    /**
     * Removes any invalid Navtex publication
     * @param message the message to check
     */
    private void checkNavtexValid(Message message) {
        // Check that the message is indeed an MSI mesasge
        Publication pub = message.getPublication(getType());
        if (pub != null && message.getSeriesIdentifier().getMainType() != SeriesIdType.MSI) {
            message.getPublications().remove(pub);
        }
    }

    /**
     * Checks that the system mail list used by the MailPublisher exists.
     * If not, create it
     */
    @PostConstruct
    public void checkCreateMailListTemplates() {

        try {
            // First check that the template exists
            MailListTemplate template = mailListService.findTemplateByName(TEMPLATE_NAVTEX_NAME);
            if (template == null) {
                template = new MailListTemplate();
                template.setName(TEMPLATE_NAVTEX_NAME);
                template.setType(NAVTEX_PUBLISHER_TYPE);
                template.setTemplate("maillist-navtex.ftl");
                template.setBundle("MailListMessageDetails");
                template.setCollated(false);
                template = mailListService.saveMailListTemplate(template);
            }

            // Check that the mail list exists for each transmitter
            for (String transmitter : transmitters) {
                String name = MAIL_LIST_NAME_PREFIX + transmitter;
                MailList mailList = mailListService.findMailListByTemplateAndName(template, name);
                if (mailList == null) {
                    mailList = new MailList();
                    mailList.setName(name);
                    mailList.setTemplate(template);
                    mailList.setChangedMessages(true);
                    mailList.setSchedule(MailList.Schedule.CONTINUOUS);
                    mailList.setSendIfEmpty(false);
                    mailList.setPublicMailingList(false);

                    MessageSearchParams filter = new MessageSearchParams();
                    filter.setStatus(Status.PUBLISHED);
                    mailListService.updateFilter(mailList, filter, "en");
                    mailListService.createMailList(mailList);
                }
            }
        } catch (Exception e) {
            log.error("Failed creating mail list " + e, e);
        }
    }

    /**
     * Returns a cache of Navtex data used internally
     * @param mailData the mail Freemarker data
     * @return the map
     */
    @SuppressWarnings("unchecked")
    private Map<Integer, NavtexData> getNavtexDataMap(Map<String, Object> mailData) {
        Map<Integer, NavtexData> navtexData = (Map<Integer, NavtexData>)mailData.get("navtexData");
        if (navtexData == null) {
            navtexData = new HashMap<>();
            mailData.put("navtexData", navtexData);
        }
        return navtexData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkIncudeMessage(MailList mailList, MessageVo messageVo, PublicationVo publicationVo, Map<String, Object> mailData) {
        try {
            NavtexData data = JsonUtils.fromJson(publicationVo.getData(), NavtexData.class);

            // We cache the Navtex data in the mailData
            getNavtexDataMap(mailData).put(messageVo.getId(), data);

            // Check that there is valid NAVTEX message
            if (StringUtils.isBlank(data.getMessage())) {
                return false;
            }

            // Determine the transmitters to send to
            List<String> transmitters = data.getTransmitter().entrySet().stream()
                    .filter(Map.Entry::getValue)
                    .map(t -> t.getKey().toLowerCase())
                    .collect(Collectors.toList());

            // Check that the transmitter name is part of the mailing list name
            // TODO: Improve robustness
            return transmitters.stream().anyMatch(t -> mailList.getName().toLowerCase().contains(t));

        } catch (IOException e) {
            // Exclude message, if we cannot process the NAVTEX data
            log.warn("Could not extract transmitter from publication " + publicationVo);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String generatedMailContent(MailList mailList, String lang, Map<String, Object> mailData) throws Exception {

        // Navtex templates are not collated, and thus, there will be a single message associated with the mailData
        MessageVo message = (MessageVo) mailData.get("message");
        NavtexData data = getNavtexDataMap(mailData).get(message.getId());

        // Store the Navtex data in the "navtexData" mail data attribute
        mailData.put("navtexData", data);

        // Generate mail content via a Freemarker transformation
        return super.generatedMailContent(mailList, lang, mailData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void sendMail(String content, MailList mailList, User user, List<MessageVo> messages) throws Exception {
        // Compose a Navtex-specific subject
        String subject = mailList.getName() + ": " + messages.get(0).getSeriesIdentifier().getFullId();
        mailService.sendMail(content, subject, app.getBaseUri(), user.getEmail());
    }

    /**
     * Called periodically to process the mailing list
     */
    @Schedule(persistent = false, second = "22", minute = "*/3", hour = "*", dayOfWeek = "*", year = "*")
    public void periodicProcessPendingMailingLists() {
        processPendingMailLists();
    }

    // *******************************
    // Helper classes
    // *******************************

    /**
     * Navtex Priority
     */
    public enum NavtexPriority {
        NONE,
        ROUTINE,
        IMPORTANT,
        VITAL
    }

    /**
     * Navtex Data
     */
    public static class NavtexData implements Serializable {
        Map<String, Boolean> transmitter = new HashMap<>();
        NavtexPriority priority;
        String message;

        public Map<String, Boolean> getTransmitter() {
            return transmitter;
        }

        public void setTransmitter(Map<String, Boolean> transmitter) {
            this.transmitter = transmitter;
        }

        public NavtexPriority getPriority() {
            return priority;
        }

        public void setPriority(NavtexPriority priority) {
            this.priority = priority;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
