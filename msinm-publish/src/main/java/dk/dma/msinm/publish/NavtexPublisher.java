package dk.dma.msinm.publish;

import dk.dma.msinm.common.settings.annotation.Setting;
import dk.dma.msinm.common.templates.TemplateContext;
import dk.dma.msinm.common.templates.TemplateType;
import dk.dma.msinm.common.util.JsonUtils;
import dk.dma.msinm.model.MailList;
import dk.dma.msinm.model.MailListTemplate;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.model.Publication;
import dk.dma.msinm.model.SeriesIdType;
import dk.dma.msinm.model.SeriesIdentifier;
import dk.dma.msinm.model.Status;
import dk.dma.msinm.service.MessageSearchParams;
import dk.dma.msinm.user.User;
import dk.dma.msinm.vo.MessageVo;
import dk.dma.msinm.vo.PublicationVo;
import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

/**
 * Defines a standard NAVTEX publisher that handles publishing of messages via NAVTEX.
 */
@Singleton
@Startup
@Lock(LockType.READ)
@Path("/publisher/navtex")
public class NavtexPublisher extends BaseMailPublisher {

    public static final String NAVTEX_PUBLISHER_TYPE = "navtex";
    public static final String NAVTEX_LANG = "en";

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
     * Creates a template Navtex publication
     * @param publish whether to publish or not
     * @param message the message
     * @return the template Navtex publication
     */
    protected PublicationVo createNavtexPublication(boolean publish, String message) {
        PublicationVo publication = new PublicationVo();
        publication.setType(getType());
        publication.setPublish(publish);

        // Enable Navtex for MSI messages only
        publication.setMessageTypes(Collections.singleton(SeriesIdType.MSI.name()));

        NavtexData data = new NavtexData();
        for (String transmitter : transmitters) {
            data.getTransmitter().put(transmitter, Boolean.FALSE);
        }
        data.setPriority(NavtexPriority.ROUTINE);
        data.setMessage(message);
        try {
            publication.setData(JsonUtils.toJson(data));
        } catch (IOException e) {
            log.warn("Failed formatting publication data " + data);
        }
        return publication;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void newTemplateMessage(MessageVo messageVo) {
        messageVo.checkCreatePublications().add(createNavtexPublication(false, ""));
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
     * {@inheritDoc}
     */
    @Override
    public void setStatus(Message message) {
        checkNavtexPublication(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getFieldTemplateLanguages() {
        // Returns the Navtex language
        return new String[] { NAVTEX_LANG };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFieldTemplateResult(MessageVo messageVo, String result, String language) {
        PublicationVo pub = messageVo.getPublication(getType());
        if (pub != null) {
            try {
                NavtexData data = JsonUtils.fromJson(pub.getData(), NavtexData.class);
                data.setMessage(result);
                pub.setData(JsonUtils.toJson(data));
            } catch (IOException e) {
                log.debug("Could not update Navtex message with field template result");
            }
        }
    }


    /**
     * Check if the Navtex publication needs have a placeholder id updated
     * @param message the message to check
     */
    private void checkNavtexPublication(Message message) {
        Publication pub = message.getPublication(getType());
        if (pub != null && message.getSeriesIdentifier().getNumber() != null) {

            try {
                NavtexData data = JsonUtils.fromJson(pub.getData(), NavtexData.class);
                String navtexMessage = data.getMessage();

                SeriesIdentifier placeHolderId = message.getSeriesIdentifier().copy();
                placeHolderId.setNumber(null);
                if (navtexMessage.contains(placeHolderId.getFullId())) {
                    navtexMessage = navtexMessage.replace(placeHolderId.getFullId(), message.getSeriesIdentifier().getFullId());
                    data.setMessage(navtexMessage);
                    pub.setData(JsonUtils.toJson(data));
                }
            } catch (IOException e) {
                log.debug("Could not update series number in Twitter message");
            }
        }
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
                    mailListService.updateFilter(mailList, filter, NAVTEX_LANG);
                    mailListService.createMailList(mailList);
                }
            }
        } catch (Exception e) {
            log.error("Failed creating mail list " + e, e);
        }
    }

    // ***************************************
    // *** Generate Navtex template
    // ***************************************

    /**
     * Composes a Navtex message from the given message
     *
     * @param msg the message
     * @return the publication
     */
    @POST
    @Path("/generate")
    @Consumes("application/json")
    @Produces("application/json")
    @GZIP
    @NoCache
    public PublicationVo generateNavtexMessage(MessageVo msg) throws Exception {

        // Prefer English
        msg.sortDescsByLang(NAVTEX_LANG);

        // Get or create the Navtext publication and Navtex data
        PublicationVo pub = msg.getPublication(getType());
        if (pub == null) {
            pub = createNavtexPublication(true, "");
        }
        NavtexData navtexData = JsonUtils.fromJson(pub.getData(), NavtexData.class);

        // Generate Navtex message
        Map<String, Object> data = new HashMap<>();
        data.put("msg", msg);
        data.put("pub", pub);
        data.put("navtex", navtexData);

        SimpleDateFormat navtexUtcDate = new SimpleDateFormat("ddHHmm 'UTC' MMM yy", Locale.US);
        navtexUtcDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        data.put("dateFormat", navtexUtcDate);

        TemplateContext ctx = templateService.getTemplateContext(
                TemplateType.MESSAGE,
                "navtex-message.ftl",
                data,
                NAVTEX_LANG,
                null);
        String navtexMessage = templateService.process(ctx);
        navtexData.setMessage(navtexMessage);
        pub.setData(JsonUtils.toJson(navtexData));

        return pub;
    }

    // ***************************************
    // *** Mail support
    // ***************************************

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
