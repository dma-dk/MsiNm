package dk.dma.msinm.publish;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dk.dma.msinm.common.settings.DefaultSetting;
import dk.dma.msinm.common.settings.Setting;
import dk.dma.msinm.common.settings.Settings;
import dk.dma.msinm.common.templates.TemplateContext;
import dk.dma.msinm.common.templates.TemplateService;
import dk.dma.msinm.common.templates.TemplateType;
import dk.dma.msinm.common.util.JsonUtils;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.model.Publication;
import dk.dma.msinm.model.SeriesIdType;
import dk.dma.msinm.model.SeriesIdentifier;
import dk.dma.msinm.service.MessageService;
import dk.dma.msinm.service.Publisher;
import dk.dma.msinm.vo.MessageVo;
import dk.dma.msinm.vo.PublicationVo;
import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines a audion broadcasting publisher that handles publishing of messages via radio.
 */
@Singleton
@Startup
@Lock(LockType.READ)
@Path("/publisher/audio")
public class AudioPublisher extends Publisher {

    public static final String AUDIO_PUBLISHER_TYPE = "audio";
    public static final Setting AUDIO_LANG = new DefaultSetting("publishAudioLanguage", "da");

    @Inject
    Logger log;

    @Inject
    MessageService messageService;

    @Inject
    TemplateService templateService;

    @Inject
    Settings settings;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return AUDIO_PUBLISHER_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {
        return 15;
    }

    /**
     * Creates a template Audio publication
     * @param publish whether to publish or not
     * @param message the message
     * @return the template audio publication
     */
    protected PublicationVo createAudioPublication(boolean publish, String message) {
        PublicationVo publication = new PublicationVo();
        publication.setType(getType());
        publication.setPublish(publish);

        // Enable Audio for MSI messages only
        publication.setMessageTypes(Collections.singleton(SeriesIdType.MSI.name()));

        AudioData data = new AudioData();
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
        messageVo.checkCreatePublications().add(createAudioPublication(false, ""));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createMessage(Message message) {
        checkAudioValid(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateMessage(Message message) {
        checkAudioValid(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStatus(Message message) {
        checkAudioPublication(message);
    }

    /**
     * Check if the Audio publication needs have a placeholder id updated
     * @param message the message to check
     */
    private void checkAudioPublication(Message message) {
        Publication pub = message.getPublication(getType());
        if (pub != null && message.getSeriesIdentifier().getNumber() != null) {

            try {
                AudioData data = JsonUtils.fromJson(pub.getData(), AudioData.class);
                String audioMessage = data.getMessage();

                SeriesIdentifier placeHolderId = message.getSeriesIdentifier().copy();
                placeHolderId.setNumber(null);
                if (audioMessage.contains(placeHolderId.getFullId())) {
                    audioMessage = audioMessage.replace(placeHolderId.getFullId(), message.getSeriesIdentifier().getFullId());
                    data.setMessage(audioMessage);
                    pub.setData(JsonUtils.toJson(data));
                }
            } catch (IOException e) {
                log.debug("Could not update series number in Audio message");
            }
        }
    }

    /**
     * Removes any invalid audio publication
     * @param message the message to check
     */
    private void checkAudioValid(Message message) {
        // Check that the message is indeed an MSI mesasge
        Publication pub = message.getPublication(getType());
        if (pub != null && message.getSeriesIdentifier().getMainType() != SeriesIdType.MSI) {
            message.getPublications().remove(pub);
        }
    }


    /**
     * Composes an audio message from the given message
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
    public PublicationVo generateAudioMessage(MessageVo msg) throws Exception {

        String language = settings.get(AUDIO_LANG);

        // Prefer audio language
        msg.sortDescsByLang(language);

        // Get or create the Audio publication and audio data
        PublicationVo pub = msg.getPublication(getType());
        if (pub == null) {
            pub = createAudioPublication(true, "");
        }
        AudioData audioData = JsonUtils.fromJson(pub.getData(), AudioData.class);

        // Generate Audio message
        Map<String, Object> data = new HashMap<>();
        data.put("msg", msg);
        data.put("pub", pub);
        data.put("posFormat", "audio");

        // Re-use Navdat template for now
        TemplateContext ctx = templateService.getTemplateContext(
                TemplateType.MESSAGE,
                "navdat-message.ftl",
                data,
                language,
                null);
        String message = templateService.process(ctx);
        audioData.setMessage(message);
        pub.setData(JsonUtils.toJson(audioData));

        return pub;
    }

    /**
     * Publish the message to the Audio receiver.
     * @param id the id of the message
     */
    public void publishMessage(Integer id) throws Exception {

        if (!isActive()) {
            return;
        }

        Message message = messageService.getCachedMessage(id);
        Publication pub = message.getPublication(getType());
        if (pub == null  || StringUtils.isBlank(pub.getData()) || !pub.isPublish()) {
            return;
        }

        AudioData data = JsonUtils.fromJson(pub.getData(), AudioData.class);

        // TODO: Proper Audio PUBLISHING
        log.info("******** PUBLISH " + message.getSeriesIdentifier().getFullId() + " to Audio **********");
        log.info(data.getMessage());
        log.info("*********************************************");
    }

    // *******************************
    // Helper classes
    // *******************************

    /**
     * Audio Data
     */
    @JsonIgnoreProperties(ignoreUnknown=true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class AudioData implements Serializable {
        String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

}
