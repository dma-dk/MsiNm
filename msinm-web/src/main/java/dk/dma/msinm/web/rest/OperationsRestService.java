package dk.dma.msinm.web.rest;

import dk.dma.msinm.common.settings.SettingsCache;
import dk.dma.msinm.service.MessageCache;
import org.jboss.ejb3.annotation.SecurityDomain;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * REST interface for various Operations functionality
 */
@Path("/admin/operations")
@Stateless
@SecurityDomain("msinm-policy")
@RolesAllowed({ "admin" })
public class OperationsRestService {

    @Inject
    private SettingsCache settingsCache;

    @Inject
    private MessageCache messageCache;

    /**
     * Resets various caches
     */
    @GET
    @Path("/reset-cache/{cacheId}")
    public String resetCache(@PathParam("cacheId") String cacheId) {
        long t0 = System.currentTimeMillis();

        if ("all".equals(cacheId) || "message".equals(cacheId)) {
            messageCache.clearCache();
        }
        if ("all".equals(cacheId) || "settings".equals(cacheId)) {
            settingsCache.clearCache();
        }

        return String.format("Cleared %s cache(s) in %d ms", cacheId, System.currentTimeMillis() - t0);
    }
}
