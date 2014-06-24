package dk.dma.msinm.web.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import dk.dma.msinm.model.Area;
import dk.dma.msinm.model.MessageLocation;
import dk.dma.msinm.model.Point;
import dk.dma.msinm.service.AreaService;
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
import javax.ws.rs.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * REST interface for accessing MSI-NM areas
 */
@Path("/admin/areas")
@Stateless
@SecurityDomain("msinm-policy")
@PermitAll
public class AreaRestService {

    @Inject
    Logger log;

    @Inject
    AreaService areaService;


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

    @DELETE
    @Path("/area/{areaId}")
    @Produces("application/json")
    @RolesAllowed({ "admin" })
    public String deleteArea(@PathParam("areaId") Integer areaId) throws Exception {
        log.info("Deleting area " + areaId);
        areaService.deleteArea(areaId);
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
            area.getOrCreateDesc("da").setName(nameLocal);
            area.getOrCreateDesc("en").setName(nameEnglish);
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
