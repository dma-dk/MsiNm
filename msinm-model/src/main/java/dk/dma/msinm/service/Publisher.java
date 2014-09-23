package dk.dma.msinm.service;

import dk.dma.msinm.common.MsiNmApp;
import dk.dma.msinm.common.mail.MailService;
import dk.dma.msinm.common.settings.DefaultSetting;
import dk.dma.msinm.common.settings.Settings;
import dk.dma.msinm.common.settings.SettingsEntity;
import dk.dma.msinm.common.templates.TemplateContext;
import dk.dma.msinm.common.templates.TemplateService;
import dk.dma.msinm.common.templates.TemplateType;
import dk.dma.msinm.common.util.JsonUtils;
import dk.dma.msinm.model.MailList;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.model.Status;
import dk.dma.msinm.user.User;
import dk.dma.msinm.vo.MessageVo;
import dk.dma.msinm.vo.PublicationVo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for all publishers, such as MailPublisher, NavtexPublisher, etc...
 * <p>
 *     NB: Subclasses must be Singletons
 * </p>
 */
public abstract class Publisher {

    @Inject
    Logger log;

    @Inject
    PublishingService publishingService;

    @Inject
    Settings settings;

    @Inject
    protected MailListService mailListService;

    @Inject
    protected TemplateService templateService;

    @Inject
    protected MailService mailService;

    @Inject
    MessageSearchService messageSearchService;

    @Inject
    MsiNmApp app;

    /**
     * Registers the publisher with the PublishingService
     */
    @PostConstruct
    public void registerPublisher() {
        publishingService.registerPublisher(
                new PublisherContext(
                        getType(),
                        getClass(),
                        getPriority(),
                        isActive())
        );
    }

    /**
     * Returns a type key for the publisher. Must be unique
     * @return a type key for the publisher
     */
    public abstract String getType();

    /**
     * Returns a priority of the publisher type. Used for sorting the publishers.
     * @return a priority of the publisher type
     */
    public abstract int getPriority();

    /**
     * Updates the new message template with a publication of this publisher
     *
     * @param messageVo the new message template
     */
    public abstract void newTemplateMessage(MessageVo messageVo);

    /**
     * Prior to creating a new message, let the publisher check up on publication.
     * Default implementation does nothing.
     * @param message the message about to be created
     */
    public void createMessage(Message message) {
    }

    /**
     * Prior to updating an existing message, let the publisher check up on the publication.
     * Default implementation does nothing.
     * @param message the message about to be updated
     */
    public void updateMessage(Message message) {
    }

    /**
     * Returns the key for the active setting of this publisher
     * @return the key for the active setting of this publisher
     */
    public String getActiveSettingKey() {
        return String.format("publish%sActive", StringUtils.capitalize(getType()));
    }

    /**
     * Returns whether or not this publisher is active
     * @return whether or not this publisher is active
     */
    public boolean isActive() {
        return settings.getBoolean(
                new DefaultSetting(getActiveSettingKey(), "false"));
    }

    /**
     * Sets whether or not this publisher is active
     * @param active whether or not this publisher is active
     */
    public void setActive(boolean active) {
        if (active != isActive()) {
            settings.updateSetting(new SettingsEntity(
                    getActiveSettingKey(),
                    String.valueOf(active)));
            // Re-register the publisher
            registerPublisher();
        }
    }

    //*********************************
    //****** Mail utility methods *****
    //*********************************

    /**
     * Publishers that support mail lists can call this method to process pending mail lists
     */
    public void processPendingMailLists() {

        // Check that the publisher is active
        if (!isActive()) {
            return;
        }

        // Get the mail lists for this publication type
        List<MailList> pendingMailLists = mailListService.findPendingMailListsOfType(getType());
        for (MailList mailList : pendingMailLists) {
            log.info("Processing mail list " + mailList.getName() + " of type " + getType());
            Date executionTime = new Date();

            try {
                // Get the search filter
                MessageSearchParams params;
                if (StringUtils.isBlank(mailList.getFilter())) {
                    params = new MessageSearchParams();
                    params.setStatus(Status.PUBLISHED);
                } else {
                    params = JsonUtils.fromJson(mailList.getFilter(), MessageSearchParams.class);
                }

                // Policy restrictions
                params.setMaxHits(100);

                // Restrict the updated time interval
                params.setUpdatedTo(executionTime);
                if (mailList.isChangedMessages() && mailList.getLastExecution() != null) {
                    // The search use from <= updated <= to. Add 1 ms to the from time to avoid overlaps.
                    params.setUpdatedFrom(new Date(mailList.getLastExecution().getTime() + 1));
                }

                // Get the search result
                MessageSearchResult result = messageSearchService.search(params);

                // Extract the message that are set to be published
                List<MessageVo> messages = result.getMessages();
                messages.removeIf(msg -> {
                    PublicationVo pub = msg.getPublication(getType());
                    return  (pub == null || !pub.isPublish());
                });

                // Check if there is anything to send
                if (result.getTotal() == 0 && !mailList.isSendIfEmpty()) {
                    continue;
                }

                processPendingMailList(mailList, messages);

            } catch (IOException e) {
                log.error("Failed executing mailing list " + mailList.getName());
            }

            // Update the last execution time and compute next execution time
            mailListService.updateExecutionTime(mailList, executionTime);
        }
    }

    /**
     * Processes the given mailing list, which matches the list of messages
     * @param mailList the mailing list
     * @param messages the messages matching the mailing list search filter
     */
    protected void processPendingMailList(MailList mailList, List<MessageVo> messages) {
        log.info("Sending mails for " + mailList.getName() + " with messages " + messages);

        Map<String, Object> data = new HashMap<>();
        data.put("total", messages.size());
        data.put("mailList", mailList);
        data.put("areaHeadings", false);

        // Send either as collated or message-by-message
        if (mailList.getTemplate().isCollated()) {
            data.put("messages", messages);
            sendMail(data, mailList, messages);
        } else {
            for (MessageVo message : messages) {
                data.put("message", message);
                sendMail(data, mailList, Arrays.asList(message));
            }
        }
    }

    /**
     * Sends an email to all mailing list recipients
     * @param data the email data
     * @param mailList the mailing list
     * @param messages the messages to send - used for language sorting
     */
    protected void sendMail(Map<String, Object> data, MailList mailList, List<MessageVo> messages) {

        for (User user : mailList.getRecipients()) {
            String lang = app.getLanguage(user.getLanguage());

            // Sort message descriptors by language
            messages.forEach(msg -> msg.sortDescsByLang(lang));

            data.put("name", user.getName());
            data.put("email", user.getEmail());
            try {
                // Generate mail body
                TemplateContext ctx = templateService.getTemplateContext(
                        TemplateType.MAIL,
                        mailList.getTemplate().getTemplate(),
                        data,
                        lang,
                        mailList.getTemplate().getBundle());
                String content = templateService.process(ctx);

                // Send the email
                String subject = "MSI-NM Mailing list: " + mailList.getName();
                mailService.sendMail(content, subject, app.getBaseUri(), user.getEmail());
            } catch (Exception e) {
                log.error("Error sending mail list " + mailList.getName() + " to user " + user.getEmail() + ": " + e, e);
            }
        }
    }


    //*********************************
    //******** Helper classes *********
    //*********************************

    /**
     * The publisher context must be registered by each publisher with the PublishingService.
     */
    public static class PublisherContext {
        String type;
        Class<? extends Publisher> publisherClass;
        int priority;
        boolean active;

        /**
         * No-arg constructor
         */
        public PublisherContext() {
        }

        /**
         * Constructor
         * @param type the type
         * @param publisherClass the publisher class
         * @param priority the priority
         */
        public PublisherContext(String type, Class<? extends Publisher> publisherClass, int priority, boolean active) {
            this.type = type;
            this.publisherClass = publisherClass;
            this.priority = priority;
            this.active = active;
        }

        public String getType() {
            return type;
        }

        public Class<? extends Publisher> getPublisherClass() {
            return publisherClass;
        }

        public int getPriority() {
            return priority;
        }

        public boolean isActive() {
            return active;
        }
    }
}
