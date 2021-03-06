/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.msinm.publish;

import dk.dma.msinm.common.MsiNmApp;
import dk.dma.msinm.common.model.DataFilter;
import dk.dma.msinm.common.repo.RepoFileVo;
import dk.dma.msinm.common.settings.DefaultSetting;
import dk.dma.msinm.common.settings.Setting;
import dk.dma.msinm.common.settings.Settings;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.service.MessageSearchParams;
import dk.dma.msinm.service.MessageSearchResult;
import dk.dma.msinm.service.MessageSearchService;
import dk.dma.msinm.service.MessageService;
import dk.dma.msinm.service.Publisher;
import dk.dma.msinm.vo.MessageVo;
import dma.msinm.AbstractMCMsiNmService;
import dma.msinm.MCMessage;
import dma.msinm.MCMsiNmUpdatesBroadcast;
import dma.msinm.MCSearchResult;
import net.maritimecloud.core.id.MaritimeId;
import net.maritimecloud.core.id.MmsiId;
import net.maritimecloud.net.MessageHeader;
import net.maritimecloud.net.mms.MmsClient;
import net.maritimecloud.net.mms.MmsClientConfiguration;
import net.maritimecloud.net.mms.MmsConnection;
import net.maritimecloud.net.mms.MmsConnectionClosingCode;
import net.maritimecloud.util.Timestamp;
import net.maritimecloud.util.geometry.Position;
import net.maritimecloud.util.geometry.PositionReader;
import net.maritimecloud.util.geometry.PositionTime;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Main interface to the Maritime Cloud service
 */
@Singleton
@Lock(LockType.READ)
@Startup
public class MaritimeCloudPublisher extends Publisher {

    public static final String CLOUD_PUBLISHER_TYPE = "maritime cloud";
    public static final String CLOUD_SERVICE_NAME   = "MSI-NM";

    private static final Setting CLOUD_HOST     = new DefaultSetting("cloudHost", "mms.sandbox03.maritimecloud.net");
    private static final Setting CLOUD_ID       = new DefaultSetting("cloudId", "999000007");
    private static final Setting CLOUD_POS      = new DefaultSetting("cloudLatLonPos", "55.6546523 12.5144583");
    private static final Setting CLOUD_DEBUG    = new DefaultSetting("cloudDebug", "false");

    @Inject
    private Logger log;

    @Inject
    Settings settings;

    @Inject
    MsiNmApp app;

    @Inject
    MessageService messageService;

    @Inject
    MessageSearchService messageSearchService;

    MmsClient mmsClient;

    String host;

    MaritimeId maritimeId;

    Position position;

    boolean debug;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return CLOUD_PUBLISHER_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {
        return 300;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void newTemplateMessage(MessageVo messageVo) {
        // No UI used for maritime cloud publishing.
    }

    /**
     * Read the configuration and start the cloud mmsClient
     */
    @PostConstruct
    public void init() {
        readSettings();
    }

    /**
     * Stop the Cloud mmsClient
     */
    @PreDestroy
    public void closeConnection() {
        try {
            if (connected()) {
                mmsClient.close();
                mmsClient.awaitTermination(2, TimeUnit.SECONDS);
                mmsClient = null;
                log.info("Closed cloud mmsClient");
            }
        } catch (Exception e) {
            log.error("Error terminating cloud mmsClient");
        }
    }

    /**
     * Returns if the service is connected to the maritime cloud
     * @return if the service is connected to the maritime cloud
     */
    public boolean connected() {
        return mmsClient != null;
    }

    /**
     * Called every minute to check the cloud status
     */
    @Schedule(persistent=false, second="50", minute="*", hour="*", dayOfWeek="*", year="*")
    public void checkCloudStatus() {
        debug = settings.getBoolean(CLOUD_DEBUG);
        if (!connected() && isActive()) {
            initCloudConnection();
        } else if (connected() && !isActive()) {
            closeConnection();
        }
    }

    /**
     * Broadcast the message over the maritime cloud
     * @param message the message to broadcast
     */
    public void broadcast(MCMessage message) {
        if (connected()) {
            MCMsiNmUpdatesBroadcast broadcast = new MCMsiNmUpdatesBroadcast();
            broadcast.setMsg(message);
            mmsClient.broadcast(broadcast);
        }
    }

    /**
     * (Re-)reads the settings
     */
    void readSettings() {
        debug = settings.getBoolean(CLOUD_DEBUG);
        host = settings.get(CLOUD_HOST);
        maritimeId = new MmsiId((int)settings.getLong(CLOUD_ID));
        String[] latLon = settings.get(CLOUD_POS).split(" ");
        position = Position.create(Double.valueOf(latLon[0]), Double.valueOf(latLon[1]));

        log.info(String.format("Read cloud settings: host=%s, id=%s, pos=%s", host, maritimeId, position));
    }

    /**
     * Initialize the Maritime Cloud mmsClient
     * @return success or failure
     */
    @Lock(LockType.WRITE)
    private boolean initCloudConnection() {
        log.info("Connecting to cloud server: " + host + " with maritime id " + maritimeId);

        MmsClientConfiguration mmsConf = MmsClientConfiguration.create(maritimeId);
        mmsConf.properties().setName(CLOUD_SERVICE_NAME);
        mmsConf.properties().setOrganization(app.getOrganizationName());
        mmsConf.properties().setDescription("MSI and NM T&P messages from the " + app.getOrganizationName());

        // Hook up a position reader
        mmsConf.setPositionReader(new PositionReader() {
            @Override
            public PositionTime getCurrentPosition() {
                return PositionTime.create(
                        position.getLatitude(),
                        position.getLongitude(),
                        System.currentTimeMillis());
            }
        });

        // Check if we need to log the MaritimeCloudConnection activity
        mmsConf.addListener(new MmsConnection.Listener() {
            @Override
            public void connecting(URI host) {
                if (debug) {
                    log.info("Connecting to " + host);
                }
            }

            @Override
            public void connected(URI host) {
                if (debug) {
                    log.info("Connected to " + host);
                }
            }

            @Override
            public void binaryMessageReceived(byte[] message) {
                if (debug) {
                    log.info("Received binary message: " + (message == null ? 0 : message.length) + " bytes");
                }
            }

            @Override
            public void binaryMessageSend(byte[] message) {
                if (debug) {
                    log.info("Sending binary message: " + (message == null ? 0 : message.length) + " bytes");
                }
            }

            @Override
            public void textMessageReceived(String message) {
                if (debug) {
                    log.info("Received text message: " + message);
                }
            }

            @Override
            public void textMessageSend(String message) {
                if (debug) {
                    log.info("Sending text message: " + message);
                }
            }

            @Override
            public void disconnected(MmsConnectionClosingCode closeReason) {
                if (debug) {
                    log.info("Disconnected with reason: " + closeReason);
                }
            }
        });

        try {
            mmsConf.setHost(host);
            mmsClient = mmsConf.build();

            if (mmsClient != null) {
                log.info("Connected successfully to cloud server: " + host + " with shipId " + maritimeId);

                // Register the MCMsiNmService endpoint
                registerCloudService();

                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("Failed to connect to server: " + e);
            return false;
        }
    }

    /**
     * Register the maritime cloud MSI-NM service
     */
    private void registerCloudService() {
        try {
            mmsClient.endpointRegister(new AbstractMCMsiNmService() {
                @Override
                protected MCSearchResult activeMessages(MessageHeader header, String lang) {
                    return getActiveMessages(lang);
                }

                @Override
                protected MCSearchResult activeMessagesIfUpdates(MessageHeader header, String lang, Timestamp date) {
                    return getActiveMessagesIfUpdates(lang, date);
                }
            }).awaitRegistered(4, TimeUnit.SECONDS);

        } catch (InterruptedException e) {
            log.error("Error hooking up services", e);
        }

    }

    /**
     * Returns the list of active messages converted to MCMessage messages
     * @param lang the language
     * @return the list of active messages converted to MCMessage messages
     */
    private MCSearchResult getActiveMessages(String lang) {
        return getActiveMessagesIfUpdates(lang, null);
    }

    /**
     * Returns the list of active messages converted to MCMessage messages if there are any updates after the given timestamp
     * @param lang the language
     * @param date the threshold timestamp
     * @return the list of active messages converted to MCMessage messages or null if no updates after the given timestamp
     */
    private MCSearchResult getActiveMessagesIfUpdates(String lang, Timestamp date) {

        MCSearchResult result = new MCSearchResult();
        result.setSearchTime(Timestamp.now());

        // Compute the last update time.
        // This is the last updated time of all published, cancelled and expired messages.
        Date lastUpdated = messageService.findLastUpdated();
        result.setLastUpdate(lastUpdated == null ? null : Timestamp.create(lastUpdated.getTime()));

        // Check if a threshold timestamp has been specified
        if (date != null && lastUpdated != null && lastUpdated.getTime() <= date.getTime()) {
            log.debug("Active message list not changed after " + date);
            result.setUnchanged(true);
            return result;
        }

        // Perform the search
        try {
            long t0 = System.currentTimeMillis();
            MessageSearchParams params = MessageSearchParams.readParams(lang, "", "PUBLISHED", "", "", "", "", "", "", "", 1000, 0, "ID", "DESC", false);
            MessageSearchResult searchResult =  messageSearchService.search(params);
            log.info(String.format("Search [%s] returns %d of %d messages in %d ms", params.toString(), searchResult.getMessages().size(), searchResult.getTotal(), System.currentTimeMillis() - t0));

            searchResult.getMessages().stream()
                    .map(this::fetchAttachments)    // Fetch message attachments
                    .map(MsdlUtils::convert)        // Convert to MCMessage
                    .map(this::externalizeMessage)  // Externalize HTML links and attachments
                    .forEach(result::addMessages);  // Add to search result
            result.setUnchanged(false);

        } catch (Exception e) {
            log.error("Error finding published messages", e);
            result.setError(e.getMessage());
        }

        return result;
    }


    /**
     * Publish the message to the maritime cloud
     * @param id the id of the message
     */
    public void publishMessage(Integer id) throws Exception {
        DataFilter filter = DataFilter.get("Message.details", "Message.firingExercise", "Area.parent", "Category.parent").setLang("en");

        Message message = messageService.getCachedMessage(id);
        MessageVo msg = new MessageVo(message, filter);

        // Fetch attachments
        msg = fetchAttachments(msg);

        // Convert to an MCMessage
        MCMessage cloudMsg = MsdlUtils.convert(msg);

        // Externalize links and attachments
        cloudMsg = externalizeMessage(cloudMsg);

        broadcast(cloudMsg);
    }


    /**
     * Fetches the list of attachments for the given message
     * @param msg the message to fetch attachments for
     * @return the message enriched with the list of attachments
     */
    private MessageVo fetchAttachments(MessageVo msg) {
        try {
            List<RepoFileVo> attachments = messageService.getMessageAttacthments(msg.getId());
            if (attachments.size() > 0) {
                msg.setAttachments(attachments);
            }
        } catch (IOException e) {
            log.debug("Failed to load message attachments: " + e);
        }
        return msg;
    }

    /**
     * Utility method that will process the message details HTML and turn all
     * images and links into absolute URL's pointing back to the MSI-NM server.
     * Also, attachments are turned into absolute URL's
     *
     * @param msg the message to process
     * @return the processed message
     */
    private MCMessage externalizeMessage(MCMessage msg) {
        try {
            if (msg.getDescs() != null) {
                msg.getDescs()
                        .forEach(desc -> desc.setDescription(messageService.externalizeHtml(desc.getDescription())));
            }
            if (msg.getAttachments() != null) {
                msg.getAttachments()
                        .forEach(att -> {
                            att.setPath(app.getBaseUri() + att.getPath());
                            att.setThumbnail(app.getBaseUri() + att.getThumbnail());
                        });
            }
        } catch (Exception e) {
            log.debug("Failed to externalize message HTML details: " + e);
        }
        return msg;
    }
}
