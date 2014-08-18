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
@RolesAllowed({ "sysadmin" })
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
