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

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.exception.InvalidShapeException;
import com.spatial4j.core.shape.Shape;
import dk.dma.msinm.common.model.BaseEntity;
import dk.dma.msinm.common.model.ILocalizable;
import dk.dma.msinm.common.model.IPreloadable;
import dk.dma.msinm.lucene.LuceneUtils;

import javax.json.*;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines a location as either a point, a circle, a polygon or a polyline.
 */
@Entity
public class Location extends BaseEntity<Integer> implements ILocalizable<LocationDesc>, IPreloadable {

    private static final long serialVersionUID = 1L;

    public enum LocationType {
        POINT, CIRCLE, POLYGON, POLYLINE
    }

    @NotNull
    @Enumerated(EnumType.STRING)
    private LocationType type;
    
    @NotNull
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "location")
    @OrderBy("index")
    private List<Point> points = new ArrayList<>();

    private Integer radius;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "entity")
    List<LocationDesc> descs = new ArrayList<>();

    public Location() {
    }

    /**
     * Converts a {@code Point} into a spatial4j {@code Point}
     * @param point the point to convert
     * @return the spatial4j point
     */
    public com.spatial4j.core.shape.Point toSpatial4jPoint(Point point) {
        return SpatialContext.GEO.makePoint(point.getLon(), point.getLat());
    }

    /**
     * Creates a WKT shape from the location withing the given spatial context
     * @return the spatial4j shape
     */
    public Shape toWkt() throws InvalidShapeException, ParseException {
        switch(type) {
            case POINT:
                if (points.size() != 1) {
                    throw new InvalidShapeException("Invalid point definition");
                }
                return toSpatial4jPoint(points.get(0));
            case CIRCLE:
                if (points.size() != 1 || radius == null) {
                    throw new InvalidShapeException("Invalid circle definition");
                }
                return SpatialContext.GEO.makeCircle(toSpatial4jPoint(points.get(0)), DistanceUtils.dist2Degrees(radius, DistanceUtils.EARTH_MEAN_RADIUS_KM));
            case POLYLINE:
            case POLYGON:
                if (points.size() == 0) {
                    throw new InvalidShapeException("Invalid " + type + " definition");
                }
                List<Point> shapePoints;
                String shape;
                if (type == LocationType.POLYGON) {
                    shape = "POLYGON ((%s))";
                    // Polygon needs to end in the start position
                    shapePoints = new ArrayList<>(points);
                    shapePoints.add(new Point(null, shapePoints.get(0).getLat(), shapePoints.get(0).getLon(), shapePoints.size()));
                } else {
                    shape = "LINESTRING (%s)";
                    shapePoints = points;
                }
                String coordinates = shapePoints.stream()
                        .map(p -> String.format("%f %f", p.getLon(), p.getLat()))
                        .collect(Collectors.joining(", "));

                return LuceneUtils.GEO.readShapeFromWkt(String.format(shape, coordinates));

            default:
                throw new InvalidShapeException("Unknown type");
        }
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

    /**
     * Parses Json into a {@code MessageLocation} entity
     * @param jsonData the Json data
     * @return the corresponding MessageLocation entity
     */
    public static List<Location> fromJson(String jsonData) {
        List<Location> locations = new ArrayList<>();

        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonData))) {
            for (JsonValue jloc : jsonReader.readArray()) {
                JsonObject jsonObject = (JsonObject)jloc;
                Location loc = new Location();
                loc.setType(LocationType.valueOf(jsonObject.getString("type")));
                if (jsonObject.containsKey("radius")) {
                    loc.setRadius(jsonObject.getInt("radius"));
                }
                for (JsonValue point : jsonObject.getJsonArray("points")) {
                    JsonObject pt = (JsonObject) point;
                    loc.getPoints().add(new Point(
                            loc,
                            pt.getJsonNumber("lat").doubleValue(),
                            pt.getJsonNumber("lon").doubleValue(),
                            pt.getInt("index")));
                }
                locations.add(loc);
            }
            return locations;
        }
    }

    public Location(LocationType type) {
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
                points.get(i).setIndex(i + 1);
            }
        }       
    }
    
    @Transient
    public void addPoint(Point p) {
        p.setIndex(this.points.size() + 1);
        this.points.add(p);
    }

    @Override
    public List<LocationDesc> getDescs() {
        return descs;
    }

    @Override
    public void setDescs(List<LocationDesc> descs) {
        this.descs = descs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocationDesc createDesc(String lang) {
        LocationDesc desc = new LocationDesc();
        desc.setLang(lang);
        desc.setEntity(this);
        getDescs().add(desc);
        return desc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preload() {
        points.forEach(point -> point.preload());
        descs.forEach(desc -> {});
    }
}
