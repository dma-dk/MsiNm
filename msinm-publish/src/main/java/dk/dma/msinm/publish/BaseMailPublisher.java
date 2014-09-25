package dk.dma.msinm.publish;

import dk.dma.msinm.common.MsiNmApp;
import dk.dma.msinm.common.mail.MailService;
import dk.dma.msinm.common.templates.TemplateContext;
import dk.dma.msinm.common.templates.TemplateService;
import dk.dma.msinm.common.templates.TemplateType;
import dk.dma.msinm.common.util.JsonUtils;
import dk.dma.msinm.model.MailList;
import dk.dma.msinm.model.Status;
import dk.dma.msinm.service.MailListService;
import dk.dma.msinm.service.MessageSearchParams;
import dk.dma.msinm.service.MessageSearchResult;
import dk.dma.msinm.service.MessageSearchService;
import dk.dma.msinm.service.Publisher;
import dk.dma.msinm.user.User;
import dk.dma.msinm.vo.MessageVo;
import dk.dma.msinm.vo.PublicationVo;
import freemarker.template.TemplateException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines common functionality for mailing list-based publishers
 */
public abstract class BaseMailPublisher extends Publisher {

    @Inject
    Logger log;

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
            log.debug("Processing mail list " + mailList.getName() + " of type " + getType());
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
                Map<String, Object> mailData = new HashMap<>();
                messages.removeIf(msg -> {
                    PublicationVo pub = msg.getPublication(getType());
                    return  (pub == null || !pub.isPublish() || !checkIncudeMessage(mailList, msg, pub, mailData));
                });

                // Check if there is anything to send
                if (result.getTotal() == 0 &&
                        (!mailList.isSendIfEmpty() || !mailList.getTemplate().isCollated() || mailList.getSchedule() == MailList.Schedule.CONTINUOUS)) {
                    continue;
                }

                processPendingMailList(mailList, messages, mailData);

            } catch (IOException e) {
                log.error("Failed executing mailing list " + mailList.getName());
            }

            // Update the last execution time and compute next execution time
            mailListService.updateExecutionTime(mailList, executionTime);
        }
    }

    /**
     * Can be overridden to include or exclude a message from the mailing list messages.
     * Can also be used by subclasses to prepare data for the mail Freemarker transformation
     *
     * @param mailList the mailing list
     * @param messageVo the message
     * @param publicationVo the publication
     * @param mailData data for the mail Freemarker transformation.
     * @return if the message should be included
     */
    protected boolean checkIncudeMessage(MailList mailList, MessageVo messageVo, PublicationVo publicationVo, Map<String, Object> mailData) {
        return true;
    }

    /**
     * Processes the given mailing list, which matches the list of messages
     * @param mailList the mailing list
     * @param messages the messages matching the mailing list search filter
     * @param mailData data for the mail Freemarker transformation.
     */
    protected void processPendingMailList(MailList mailList, List<MessageVo> messages, Map<String, Object> mailData) {
        log.info("Sending mails for " + mailList.getName() + " with messages " + messages);

        mailData.put("total", messages.size());
        mailData.put("mailList", mailList);
        mailData.put("areaHeadings", false);

        // Send either as collated or message-by-message
        if (mailList.getTemplate().isCollated()) {
            mailData.put("messages", messages);
            sendMail(mailList, messages, mailData);
        } else {
            for (MessageVo message : messages) {
                mailData.put("message", message);
                sendMail(mailList, Arrays.asList(message), mailData);
            }
        }
    }

    /**
     * Sends an email to all mailing list recipients
     * @param mailList the mailing list
     * @param messages the messages to send - used for language sorting
     * @param mailData data for the mail Freemarker transformation.
     */
    protected void sendMail(MailList mailList, List<MessageVo> messages, Map<String, Object> mailData) {

        for (User user : mailList.getRecipients()) {
            String lang = app.getLanguage(user.getLanguage());

            // Sort message descriptors by language
            messages.forEach(msg -> msg.sortDescsByLang(lang));

            mailData.put("name", user.getName());
            mailData.put("email", user.getEmail());
            try {
                // Generate mail body
                String content = generatedMailContent(mailList, lang, mailData);

                // Send the email
                sendMail(content, mailList, user, messages);
            } catch (Exception e) {
                log.error("Error sending mail list " + mailList.getName() + " to user " + user.getEmail() + ": " + e, e);
            }
        }
    }

    /**
     * Generate the message content using a Freemarker transformation
     * @param mailList the mailing list
     * @param lang the user language
     * @param mailData data for the mail Freemarker transformation.
     * @return the mail content
     */
    protected String generatedMailContent(MailList mailList, String lang, Map<String, Object> mailData) throws Exception {
        // Generate mail body
        TemplateContext ctx = templateService.getTemplateContext(
                TemplateType.MAIL,
                mailList.getTemplate().getTemplate(),
                mailData,
                lang,
                mailList.getTemplate().getBundle());
        return templateService.process(ctx);
    }

    /**
     * Sends an email to all mailing list recipients. Override to e.g. set a non-default subject
     * @param content the email content
     * @param mailList the mailing list
     * @param user the user to send to
     * @param messages the messages to send - used for language sorting
     */
    protected void sendMail(String content, MailList mailList, User user, List<MessageVo> messages) throws Exception {
        String subject = "MSI-NM Mailing list: " + mailList.getName();
        mailService.sendMail(content, subject, app.getBaseUri(), user.getEmail());
    }

}
