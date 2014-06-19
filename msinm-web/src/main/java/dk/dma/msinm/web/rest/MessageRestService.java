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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import dk.dma.msinm.model.Area;
import dk.dma.msinm.model.MessageLocation;
import dk.dma.msinm.model.MessageStatus;
import dk.dma.msinm.model.MessageType;
import dk.dma.msinm.model.Point;
import dk.dma.msinm.service.AreaService;
import dk.dma.msinm.service.MessageSearchParams;
import dk.dma.msinm.service.MessageSearchService;
import dk.dma.msinm.service.MessageService;
import org.apache.commons.lang.StringUtils;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * REST interface for accessing MSI-NM messages
 */
@Path("/message")
@Stateless
@SecurityDomain("msinm-policy")
@PermitAll
public class MessageRestService {

    @Inject
    Logger log;

    @Inject
    MessageService messageService;

    @Inject
    MessageSearchService messageSearchService;

    @Inject
    AreaService areaService;

    public MessageRestService() {
    }


    /**
     * Test method - returns all message
     * @return returns all message
     */
    @GET
    @Path("/all")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    public JsonArray getAll() {

        JsonArrayBuilder result = Json.createArrayBuilder();
        messageService.getActive().forEach(msg -> result.add(msg.toJson()));
        return result.build();
    }

    /**
     * Main search method
     */
    @GET
    @Path("/search")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    public JsonObject search(
            @QueryParam("q") String query,
            @QueryParam("status") @DefaultValue("ACTIVE") String status,
            @QueryParam("type") String type,
            @QueryParam("loc") String loc,
            @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate,
            @QueryParam("maxHits") @DefaultValue("100") int maxHits,
            @QueryParam("startIndex") @DefaultValue("0") int startIndex,
            @QueryParam("sortBy") @DefaultValue("issueDate") String sortBy,
            @QueryParam("sortOrder") @DefaultValue("desc") String sortOrder
    ) throws Exception {

        log.info(String.format(
                "Search with q=%s, status=%s, type=%s, loc=%s, from=%s, to=%s, maxHits=%d, startIndex=%d, sortBy=%s, sortOrder=%s",
                query, status, type, loc, fromDate, toDate, maxHits, startIndex, sortBy, sortOrder));

        MessageSearchParams params = new MessageSearchParams();
        params.setStartIndex(startIndex);
        params.setMaxHits(maxHits);

        try {
            params.setSortBy(MessageSearchParams.SortBy.valueOf(sortBy));
        } catch (Exception e) {
            log.debug("Failed parsing sortBy parameter " + sortBy);
        }

        try {
            params.setSortOrder(MessageSearchParams.SortOrder.valueOf(sortOrder));
        } catch (Exception e) {
            log.debug("Failed parsing sortOrder parameter " + sortOrder);
        }

        params.setQuery(query);

        if (StringUtils.isNotBlank(status)) {
            params.setStatus(MessageStatus.valueOf(status));
        }

        if (StringUtils.isNotBlank(type)) {
            for (String msgType : type.split(",")) {
                params.getTypes().add(MessageType.valueOf(msgType));
            }
        }

        if (StringUtils.isNotBlank(loc)) {
            params.setLocation(MessageLocation.fromJson(loc));
        }

        if (StringUtils.isNotBlank(fromDate)) {
            DateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            params.setFrom(sdf.parse(fromDate));
        }

        if (StringUtils.isNotBlank(toDate)) {
            DateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            params.setTo(sdf.parse(toDate));
        }

        return messageSearchService
                .search(params)
                .toJson()
                .build();
    }

    /**
     * Main search method
     */
    @GET
    @Path("/recreate-search-index")
    @RolesAllowed({ "admin" })
    public void recreateSearchIndex() {
        try {
            log.info("Recreating message search index");
            messageSearchService.recreateIndex();
        } catch (IOException e) {
            log.error("Error recreating message search index");
        }
    }

    /**
     * Returns all areas via a list of hierarchical root areas
     * @return returns all areas
     */
    @GET
    @Path("/area-roots")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    public JsonArray getAreaRoots() {

        JsonArrayBuilder result = Json.createArrayBuilder();
        areaService.getAreas().forEach(area -> result.add(area.toJson()));
        return result.build();
    }

    @POST
    @Path("/area")
    @Consumes("application/json")
    @Produces("application/json")
    @RolesAllowed({ "admin" })
    public String createArea(AreaVo areaVo) throws Exception {
        Area area = areaVo.toArea();
        log.info("Creating area " + area);
        areaService.createArea(area, areaVo.getParentId());
        return "OK";
    }

    @PUT
    @Path("/area")
    @Consumes("application/json")
    @Produces("application/json")
    @RolesAllowed({ "admin" })
    public String updateArea(AreaVo areaVo) throws Exception {
        Area area = areaVo.toArea();
        log.info("Updating area " + area);
        areaService.updateAreaData(area);
        return "OK";
    }

    @PUT
    @Path("/move-area")
    @Consumes("application/json")
    @Produces("application/json")
    @RolesAllowed({ "admin" })
    public String moveArea(MoveAreaVo moveAreaVo) throws Exception {
        log.info("Moving area " + moveAreaVo.getAreaId() + " to " + moveAreaVo.getParentId());
        areaService.moveArea(moveAreaVo.getAreaId(), moveAreaVo.getParentId());
        return "OK";
    }


    /*********************
     * Helper classes
     *********************/

    public static class MoveAreaVo implements Serializable {
        Integer areaId, parentId;

        public Integer getAreaId() {
            return areaId;
        }

        public void setAreaId(Integer areaId) {
            this.areaId = areaId;
        }

        public Integer getParentId() {
            return parentId;
        }

        public void setParentId(Integer parentId) {
            this.parentId = parentId;
        }
    }


    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class PointVo implements Serializable {
        double lat, lon;
        int index;
        String description;

        public Point toPoint() {
            Point point = new Point();
            point.setLat(lat);
            point.setLon(lon);
            point.setNum(index);
            // TODO: Description
            return point;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLon() {
            return lon;
        }

        public void setLon(double lon) {
            this.lon = lon;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class LocationVo implements Serializable {
        String type;
        String description;
        int radius;
        List<PointVo> points = new ArrayList<>();

        public MessageLocation toLocation() {
            MessageLocation location = new MessageLocation();
            location.setType(MessageLocation.LocationType.valueOf(type));
            location.setRadius(radius);
            points.forEach(pt -> location.getPoints().add(pt.toPoint()));
            // TODO: Description
            return location;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getRadius() {
            return radius;
        }

        public void setRadius(int radius) {
            this.radius = radius;
        }

        public List<PointVo> getPoints() {
            return points;
        }

        @JsonDeserialize(contentAs = PointVo.class)
        public void setPoints(List<PointVo> points) {
            this.points = points;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class AreaVo implements Serializable {
        Integer id;
        Integer parentId;
        String nameLocal, nameEnglish;
        List<LocationVo> locations = new ArrayList<>();

        public Area toArea() {
            Area area = new Area();
            area.setId(id);
            area.setNameLocal(nameLocal);
            area.setNameEnglish(nameEnglish);
            locations.forEach(loc -> area.getLocations().add(loc.toLocation()));
            return area;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }


        public Integer getParentId() {
            return parentId;
        }

        public void setParentId(Integer parentId) {
            this.parentId = parentId;
        }

        public String getNameLocal() {
            return nameLocal;
        }

        public void setNameLocal(String nameLocal) {
            this.nameLocal = nameLocal;
        }

        public String getNameEnglish() {
            return nameEnglish;
        }

        public void setNameEnglish(String nameEnglish) {
            this.nameEnglish = nameEnglish;
        }

        public List<LocationVo> getLocations() {
            return locations;
        }

        @JsonDeserialize(contentAs = LocationVo.class)
        public void setLocations(List<LocationVo> locations) {
            this.locations = locations;
        }
    }
}