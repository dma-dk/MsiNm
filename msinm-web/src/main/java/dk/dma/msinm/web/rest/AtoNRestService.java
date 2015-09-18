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

import dk.dma.msinm.model.AtoN;
import dk.dma.msinm.model.Location;
import dk.dma.msinm.service.AtoNService;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.util.List;

/**
 * REST interface for accessing AtoNs.
 */
@Path("/atons")
public class AtoNRestService {

    @Inject
    Logger log;

    @Inject
    AtoNService atonService;

    /**
     * Returns all AtoNs
     *
     * @return all AtoNs
     */
    @GET
    @Path("/all")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    public List<AtoN> getAll() {
        return atonService.getAllAtoNs();
    }

    /**
     * Returns the AtoNs within the bounds of the given locations
     *
     * @param locations the locations
     * @return the search result
     */
    @POST
    @Path("/contained-atons")
    @Consumes("application/json")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    public List<AtoN> containedAtoNs(List<Location> locations) {

        log.info(String.format("Searching for AtoNs locations=%s", locations));
        return atonService.getWithinLocation(locations);
    }

}
