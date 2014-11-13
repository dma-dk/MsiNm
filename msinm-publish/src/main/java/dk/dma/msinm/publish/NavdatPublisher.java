package dk.dma.msinm.publish;

import dk.dma.msinm.common.templates.TemplateContext;
import dk.dma.msinm.common.templates.TemplateService;
import dk.dma.msinm.common.templates.TemplateType;
import dk.dma.msinm.common.util.JsonUtils;
import dk.dma.msinm.common.vo.JsonSerializable;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines a NAVDAT publisher that handles publishing of messages via NAVDAT.
 */
@Singleton
@Startup
@Lock(LockType.READ)
@Path("/publisher/navdat")
public class NavdatPublisher extends Publisher {

    public static final String NAVDAT_PUBLISHER_TYPE = "navdat";

    @Inject
    Logger log;

    @Inject
    MessageService messageService;

    @Inject
    TemplateService templateService;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return NAVDAT_PUBLISHER_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {
        return 20;
    }

    /**
     * Creates a template Navdat publication
     * @param publish whether to publish or not
     * @param message the message
     * @return the template Navdat publication
     */
    protected PublicationVo createNavdatPublication(boolean publish, String message) {
        PublicationVo publication = new PublicationVo();
        publication.setType(getType());
        publication.setPublish(publish);

        // Enable Navdat for MSI messages only
        publication.setMessageTypes(Collections.singleton(SeriesIdType.MSI.name()));

        NavdatData data = new NavdatData();
        data.setPriority(NavdatPriority.ROUTINE);
        data.setBroadcast(NavdatBroadcast.GENERAL);
        data.setIncludeAttachments(false);
        data.setEncrypted(false);
        data.setEncryption("SHA-256");
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
        messageVo.checkCreatePublications().add(createNavdatPublication(false, ""));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createMessage(Message message) {
        checkNavdatValid(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateMessage(Message message) {
        checkNavdatValid(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStatus(Message message) {
        checkNavdatPublication(message);
    }

    /**
     * Check if the Navdat publication needs have a placeholder id updated
     * @param message the message to check
     */
    private void checkNavdatPublication(Message message) {
        Publication pub = message.getPublication(getType());
        if (pub != null && message.getSeriesIdentifier().getNumber() != null) {

            try {
                NavdatData data = JsonUtils.fromJson(pub.getData(), NavdatData.class);
                String navdatMessage = data.getMessage();

                SeriesIdentifier placeHolderId = message.getSeriesIdentifier().copy();
                placeHolderId.setNumber(null);
                if (navdatMessage.contains(placeHolderId.getFullId())) {
                    navdatMessage = navdatMessage.replace(placeHolderId.getFullId(), message.getSeriesIdentifier().getFullId());
                    data.setMessage(navdatMessage);
                    pub.setData(JsonUtils.toJson(data));
                }
            } catch (IOException e) {
                log.debug("Could not update series number in Navdat message");
            }
        }
    }

    /**
     * Removes any invalid Navdat publication
     * @param message the message to check
     */
    private void checkNavdatValid(Message message) {
        // Check that the message is indeed an MSI mesasge
        Publication pub = message.getPublication(getType());
        if (pub != null && message.getSeriesIdentifier().getMainType() != SeriesIdType.MSI) {
            message.getPublications().remove(pub);
        }
    }


    /**
     * Composes a Navdat message from the given message
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
    public PublicationVo generateNavdatMessage(MessageVo msg) throws Exception {

        // Prefer English
        msg.sortDescsByLang("en");

        // Get or create the Navdat publication and Navdat data
        PublicationVo pub = msg.getPublication(getType());
        if (pub == null) {
            pub = createNavdatPublication(true, "");
        }
        NavdatData navdatData = JsonUtils.fromJson(pub.getData(), NavdatData.class);

        // Generate Navdat message
        Map<String, Object> data = new HashMap<>();
        data.put("msg", msg);
        data.put("pub", pub);
        data.put("posFormat", "navtex");

        TemplateContext ctx = templateService.getTemplateContext(
                TemplateType.MESSAGE,
                "navdat-message.ftl",
                data,
                "en",
                null);
        String navdatMessage = templateService.process(ctx);
        navdatData.setMessage(navdatMessage);
        pub.setData(JsonUtils.toJson(navdatData));

        return pub;
    }

    /**
     * Publish the message to Navdat.
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

        NavdatData data = JsonUtils.fromJson(pub.getData(), NavdatData.class);

        // TODO: Proper NAVDAT PUBLISHING
        log.info("******** PUBLISH " + message.getSeriesIdentifier().getFullId() + " to Navdat **********");
        log.info("Broadcast: " + data.getBroadcast());
        log.info("Areas: " + data.getAreas());
        log.info("MMSI: " + data.getMmsi());
        log.info("Message: " + data.getMessage());
        log.info("Include attachments: " + data.getIncludeAttachments());
        log.info("Encrypted: " + data.getEncrypted());
        log.info("Encryption: " + data.getEncryption());
        log.info("*********************************************");
    }

    // *******************************
    // Helper classes
    // *******************************

    /**
     * Navdat Priority
     */
    public enum NavdatPriority {
        NONE,
        ROUTINE,
        IMPORTANT,
        VITAL
    }

    /**
     * Navdat Broadcast
     */
    public enum NavdatBroadcast {
        GENERAL,
        SELECTIVE,
        DEDICATED
    }

    /**
     * Navdat Data
     */
    public static class NavdatData implements JsonSerializable {
        NavdatPriority priority;
        NavdatBroadcast broadcast;
        String message;
        String areas;
        String mmsi;
        Boolean includeAttachments;
        Boolean encrypted;
        String encryption;

        public NavdatPriority getPriority() {
            return priority;
        }

        public void setPriority(NavdatPriority priority) {
            this.priority = priority;
        }

        public NavdatBroadcast getBroadcast() {
            return broadcast;
        }

        public void setBroadcast(NavdatBroadcast broadcast) {
            this.broadcast = broadcast;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getAreas() {
            return areas;
        }

        public void setAreas(String areas) {
            this.areas = areas;
        }

        public String getMmsi() {
            return mmsi;
        }

        public void setMmsi(String mmsi) {
            this.mmsi = mmsi;
        }

        public Boolean getIncludeAttachments() {
            return includeAttachments;
        }

        public void setIncludeAttachments(Boolean includeAttachments) {
            this.includeAttachments = includeAttachments;
        }

        public Boolean getEncrypted() {
            return encrypted;
        }

        public void setEncrypted(Boolean encrypted) {
            this.encrypted = encrypted;
        }

        public String getEncryption() {
            return encryption;
        }

        public void setEncryption(String encryption) {
            this.encryption = encryption;
        }
    }

}
