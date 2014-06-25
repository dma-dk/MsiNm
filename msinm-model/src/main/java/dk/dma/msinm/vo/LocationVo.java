package dk.dma.msinm.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import dk.dma.msinm.model.MessageLocation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Value object for the {@code Location} model entity
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class LocationVo implements Serializable {
    String type;
    String description;
    int radius;
    List<PointVo> points = new ArrayList<>();

    /**
     * Constructor
     */
    public LocationVo() {
    }

    /**
     * Constructor
     * @param location the location
     */
    public LocationVo(MessageLocation location) {
        type = location.getType().toString();
        radius = location.getRadius();
        location.getPoints().forEach(point -> points.add(new PointVo(point)));
        // TODO: Description
    }

    /**
     * Converts this VO to the model Location
     * @return the location
     */
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
