package dk.dma.msinm.service;

import dk.dma.msinm.common.settings.annotation.Setting;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.model.Publication;
import dk.dma.msinm.model.SeriesIdType;
import dk.dma.msinm.vo.MessageVo;
import dk.dma.msinm.vo.PublicationVo;
import org.slf4j.Logger;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines a standard NAVTEX publisher that handles publishing of messages via NAVTEX.
 */
@Singleton
@Startup
@Lock(LockType.READ)
public class NavtexPublisher extends Publisher {

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
        return "navtex";
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
            publication.setData(formatPublicationData(data));
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
