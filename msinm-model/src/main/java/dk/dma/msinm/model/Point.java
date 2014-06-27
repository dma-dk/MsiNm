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
import dk.dma.msinm.common.model.ILocalizable;
import dk.dma.msinm.common.model.IPreloadable;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines a position by its latitude, longitude and order
 */
@Entity
public class Point extends BaseEntity<Integer> implements ILocalizable<PointDesc>, IPreloadable {

    private static final long serialVersionUID = 1L;

    @ManyToOne
    @NotNull
    Location location;

    @NotNull
    private Double lat;
    
    @NotNull
    private Double lon;

    @Column(name = "num")
    @NotNull
    private Integer index;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "entity")
    List<PointDesc> descs = new ArrayList<>();

    /**
     * No-argument constructor
     */
    public Point() {
    }

    /**
     * Constructor
     * @param lat the latitude
     * @param lon the longitude
     */
    public Point(double lat, double lon) {
        this(null, lat, lon, 0);
    }

    /**
     * Constructor
     * @param location the location
     * @param lat the latitude
     * @param lon the longitude
     */
    public Point(Location location, double lat, double lon) {
        this(location, lat, lon, 0);
    }

    /**
     * Constructor
     * @param location the location
     * @param lat the latitude
     * @param lon the longitude
     * @param index the point index
     */
    public Point(Location location, Double lat, Double lon, Integer index) {
        this.lat = lat;
        this.lon = lon;
        this.index = index;
        this.location = location;
    }

    /**
     * Creates a Json representation of this entity
     * @return the Json representation
     */
    public JsonObjectBuilder toJson() {
        return Json.createObjectBuilder()
                .add("lat", lat)
                .add("lon", lon)
                .add("index", index);
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
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
    
    public Integer getIndex() {
        return index;
    }
    
    public void setIndex(Integer num) {
        this.index = num;
    }

    @Override
    public List<PointDesc> getDescs() {
        return descs;
    }

    @Override
    public void setDescs(List<PointDesc> descs) {
        this.descs = descs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PointDesc createDesc(String lang) {
        PointDesc desc = new PointDesc();
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
        descs.forEach(desc -> {});
    }
}
