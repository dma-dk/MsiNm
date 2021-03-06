package dk.dma.msinm.service;

import dk.dma.msinm.common.settings.DefaultSetting;
import dk.dma.msinm.common.settings.Settings;
import dk.dma.msinm.common.settings.SettingsEntity;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.vo.MessageVo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

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
                        isActive(),
                        getFieldTemplateLanguages())
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
     * Prior to updating the status of an existing message, let the publisher check up on the publication.
     * Default implementation does nothing.
     * @param message the message about to be updated
     */
    public void setStatus(Message message) {
    }

    /**
     * Publications can be updated from Freemarker templates. This method will return
     * the languages for which a Freemarker template result can be injected.
     * Returns null means that field templates injection is not supported
     * @return the languages for which a Freemarker template result can be injected
     */
    public String[] getFieldTemplateLanguages() {
        return null; // No field templates by default
    }

    /**
     * Publications can be updated from Freemarker templates. This method is called
     * to inject a Freemarker template result for a specific language in a message value object.
     * @param messageVo the message value object
     * @param result the field template result
     * @param language the language
     */
    public void setFieldTemplateResult(MessageVo messageVo, String result, String language) {
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
        String[] fieldTemplateLanguages;

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
        public PublisherContext(String type, Class<? extends Publisher> publisherClass, int priority, boolean active, String[] fieldTemplateLanguages) {
            this.type = type;
            this.publisherClass = publisherClass;
            this.priority = priority;
            this.active = active;
            this.fieldTemplateLanguages = fieldTemplateLanguages;
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

        public String[] getFieldTemplateLanguages() {
            return fieldTemplateLanguages;
        }
    }
}
