package dk.dma.msinm.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dk.dma.msinm.model.Point;

import java.io.Serializable;

/**
 * Value object for the {@code Point} model entity
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class PointVo implements Serializable {
    double lat, lon;
    int index;
    String description;

    /**
     * Constructor
     */
    public PointVo() {
    }

    /**
     * Constructor
     * @param point the point
     */
    public PointVo(Point point) {
        lat = point.getLat();
        lon = point.getLon();
        index = point.getNum();
        // TODO: Description
    }

    /**
     * Converts this VO to a Point
     * @return the point
     */
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
