package dk.dma.msinm.publish;

import dk.dma.msinm.common.util.JsonUtils;
import dk.dma.msinm.common.vo.JsonSerializable;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.model.Publication;
import dk.dma.msinm.service.Publisher;
import dk.dma.msinm.vo.AreaVo;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines a Twitter publisher that handles publishing of messages via Twitter.
 */
@Singleton
@Startup
@Lock(LockType.READ)
@Path("/publisher/twitter")
public class TwitterPublisher extends Publisher {

    public static final String TWITTER_PUBLISHER_TYPE = "twitter";

    @Inject
    Logger log;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return TWITTER_PUBLISHER_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {
        return 50;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void newTemplateMessage(MessageVo messageVo) {
        try {
            PublicationVo publication = getTwitterPublication(false, "");
            messageVo.checkCreatePublications().add(publication);
        } catch (IOException e) {
            log.warn("Failed formatting publication data ");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createMessage(Message message) {
        checkTwitterPublication(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateMessage(Message message) {
        checkTwitterPublication(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStatus(Message message) {
        checkTwitterPublication(message);
    }

    /**
     * Check if the Twitter publication needs to be updated
     * @param message the message to check
     */
    private void checkTwitterPublication(Message message) {
        Publication pub = message.getPublication(getType());
        if (pub != null && message.getSeriesIdentifier().getNumber() != null) {

            try {
                TwitterData data = JsonUtils.fromJson(pub.getData(), TwitterData.class);
                String twitterMessage = data.getMessage();
                // Check if we need to update the message series id
                int index1 = twitterMessage.indexOf(':');
                int index2 = twitterMessage.indexOf('?');
                if (index1 > 0 && index1 < 20 && index2 > 0 && index2 < index1) {
                    twitterMessage =
                            twitterMessage.substring(0, index2) +
                            String.format("%03d", message.getSeriesIdentifier().getNumber()) +
                            twitterMessage.substring(index2 + 1);
                    data.setMessage(twitterMessage);
                    pub.setData(JsonUtils.toJson(data));
                }
            } catch (IOException e) {
                log.debug("Could not update series number in Twitter message");
            }
        }
    }

    /**
     * Creates a template Twitter publication
     * @param publish whether to publish or not
     * @param message the message
     * @return the template Twitter publication
     */
    protected PublicationVo getTwitterPublication(boolean publish, String message) throws IOException {
        PublicationVo publication = new PublicationVo();
        publication.setType(getType());
        publication.setPublish(publish);

        TwitterData data = new TwitterData();
        data.setMessage(message);
        publication.setData(JsonUtils.toJson(data));
        return publication;
    }

    /**
     * Composes a Twitter message from the given message
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
    public PublicationVo generateTwitterMessage(MessageVo msg) throws Exception {

        // Prefer English
        msg.sortDescsByLang("en");

        // Build the title line
        String str = msg.getSeriesIdentifier().getFullId() + ": ";

        List<String> titleParts = new ArrayList<>();
        for (AreaVo area = msg.getArea(); area != null; area = area.getParent()) {
            titleParts.add(0, area.getDescs().get(0).getName());
        }
        MessageVo.MessageDescVo desc = msg.getDescs().get(0);
        if (StringUtils.isNotBlank(desc.getVicinity())) {
            titleParts.add(desc.getVicinity());
        }
        if (StringUtils.isNotBlank(desc.getTitle())) {
            titleParts.add(desc.getTitle());
        }
        str += titleParts.stream().collect(Collectors.joining(" - "));

        // Create an update message
       return getTwitterPublication(true, StringUtils.abbreviate(str, TwitterProvider.MAX_LENGTH));
    }


    /**
     * Twitter Data
     */
    public static class TwitterData implements JsonSerializable {
        String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

}
