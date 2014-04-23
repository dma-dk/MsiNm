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
package dk.dma.msinm.model;

import dk.dma.msinm.common.model.BaseEntity;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines a location as either a point, a circle, a polygon or a polyline.
 */
@Entity
public class MessageLocation extends BaseEntity<Integer> {

    private static final long serialVersionUID = 1L;

    public enum LocationType {
        POINT, CIRCLE, POLYGON, POLYLINE
    }

    @NotNull
    @Enumerated(EnumType.STRING)
    private LocationType type;
    
    @NotNull
    @ElementCollection
    @OrderBy("num")
    private List<Point> points = new ArrayList<>();
    
    private Integer radius;

    public MessageLocation() {

    }

    /**
     * Creates a Json representation of this entity
     * @return the Json representation
     */
    public JsonObjectBuilder toJson() {
        JsonArrayBuilder pointsJson = Json.createArrayBuilder();
        points.forEach(p -> pointsJson.add(p.toJson()));
        JsonObjectBuilder json = Json.createObjectBuilder()
                .add("type", type.toString())
                .add("points", pointsJson);
        if (radius != null) {
            json.add("radius", radius);
        }
        return json;
    }

    public MessageLocation(LocationType type) {
        this.type = type;
    }

    public LocationType getType() {
        return type;
    }

    public void setType(LocationType type) {
        this.type = type;
    }
    
    public Integer getRadius() {
        return radius;
    }
    
    public void setRadius(Integer radius) {
        this.radius = radius;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
        if (points != null && points.size() > 0) {
            for (int i=0; i < points.size(); i++) {
                points.get(i).setNum(i + 1);   
            }
        }       
    }
    
    @Transient
    public void addPoint(Point p) {
        p.setNum(this.points.size() + 1);
        this.points.add(p);
    }

}
