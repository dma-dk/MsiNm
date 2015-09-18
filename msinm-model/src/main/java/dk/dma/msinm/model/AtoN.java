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
import dk.dma.msinm.common.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;

/**
 * Represents an AtoN (aid to navigation)
 * <p/>
 * TODO:
 * 1) Properly model description and code
 * 2) Move name to AtoNDesc
 * 3) Create Lucene index for fast searching
 */
@Entity
@NamedQueries({
        @NamedQuery(name  = "AtoN.deleteAll",
                query = "delete from AtoN")
})
public class AtoN extends BaseEntity<Integer> {

    @NotNull
    @Column(unique = true)
    String atonUid;

    @NotNull
    String name;

    @NotNull
    String description;

    @NotNull
    String code;

    @NotNull
    private Double lat;

    @NotNull
    private Double lon;

    /**
     * Converts the associated position into a spatial4j {@code Point}
     * @return the spatial4j point
     */
    public com.spatial4j.core.shape.Point toWkt() {
        return SpatialContext.GEO.makePoint(lon, lat);
    }

    public String getAtonUid() {
        return atonUid;
    }

    public void setAtonUid(String atonUid) {
        this.atonUid = atonUid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
}
