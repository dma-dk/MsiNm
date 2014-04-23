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

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Defines a position by its latitude, longitude and order
 */
@Embeddable
public class Point implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    private Double lat;
    
    @NotNull
    private Double lon;
    
    @NotNull
    private Integer num;

    public Point() {

    }
    
    public Point(double lat, double lon) {
        this(lat, lon, 0);
    }

    public Point(Double lat, Double lon, Integer num) {
        this.lat = lat;
        this.lon = lon;
        this.num = num;
    }

    /**
     * Creates a Json representation of this entity
     * @return the Json representation
     */
    public JsonObjectBuilder toJson() {
        return Json.createObjectBuilder()
                .add("lat", lat)
                .add("lon", lon)
                .add("num", num);
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }
    
    public Integer getNum() {
        return num;
    }
    
    public void setNum(Integer num) {
        this.num = num;
    }

}
