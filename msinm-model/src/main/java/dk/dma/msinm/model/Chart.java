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

import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.SpatialRelation;
import dk.dma.msinm.common.model.VersionedEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Represents a chart
 */
@Entity
@NamedQueries({
        @NamedQuery(name  = "Chart.searchCharts",
                query = "select distinct c from Chart c where lower(c.chartNumber) like lower(:term) "
                        + "or ('' + c.internationalNumber) like lower(:term) "
                        + "or lower(c.name) like lower(:term) "
                        + "order by "
                        + "case when LOCATE(lower(:sort), lower(c.chartNumber)) = 0 and LOCATE(lower(:sort), lower(c.name)) = 0 then LOCATE(lower(:sort), '' + c.internationalNumber) "
                        + "when LOCATE(lower(:sort), lower(c.chartNumber)) = 0 then LOCATE(lower(:sort), c.name) "
                        + "else LOCATE(lower(:sort), lower(c.chartNumber)) end, chartNumber"),
        @NamedQuery(name  = "Chart.findAll",
                query = "select c from Chart c order by coalesce(scale, 99999999) asc, chartNumber"),
        @NamedQuery(name="Chart.findByChartNumber",
                query="SELECT chart FROM Chart chart where chart.chartNumber = :chartNumber")
})
public class Chart extends VersionedEntity<Integer> {

    @NotNull
    @Column(unique = true)
    String chartNumber;

    Integer internationalNumber;

    String horizontalDatum;

    Integer scale;

    String name;

    Double lowerLeftLatitude, lowerLeftLongitude;

    Double upperRightLatitude, upperRightLongitude;

    /**
     * Constructor
     */
    public Chart() {
    }

    /**
     * Constructor
     */
    public Chart(String chartNumber, Integer internationalNumber) {
        this.chartNumber = chartNumber;
        this.internationalNumber = internationalNumber;
    }

    /**
     * Determines if the bounds of this chart intersects with
     * the given location list.<br>
     * By <i>intersects</i> we include all relationships such as
     * "within", "contains" and "intersects" in the strict sense.
     *
     * @return if the locations intersects this chart
     */
    public boolean intersects(List<Location> locations) {
        Location location = toLocation();
        if (location == null || locations == null || locations.size() == 0) {
            return false;
        }

        try {
            Shape chartBounds = location.toWkt();
            for (Location loc : locations) {
                if (chartBounds.relate(loc.toWkt()) != SpatialRelation.DISJOINT) {
                    return true;
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }

    /**
     * Returns the location representing the bounds of this chart and null if undefined
     * @return the location representing the bounds of this chart and null if undefined
     */
    public Location toLocation() {
        // Check if proper bounds are defined for this chart
        if (lowerLeftLatitude == null || lowerLeftLongitude == null ||
                upperRightLatitude == null || upperRightLongitude == null) {
            return null;
        }

        // Build a location repre
        Location location = new Location();
        location.setType(Location.LocationType.POLYGON);
        location.getPoints().add(new Point(lowerLeftLatitude, lowerLeftLongitude));
        location.getPoints().add(new Point(upperRightLatitude, lowerLeftLongitude));
        location.getPoints().add(new Point(upperRightLatitude, upperRightLongitude));
        location.getPoints().add(new Point(lowerLeftLatitude, upperRightLongitude));
        return location;
    }

    public String getChartNumber() {
        return chartNumber;
    }

    public void setChartNumber(String chartNumber) {
        this.chartNumber = chartNumber;
    }

    public Integer getInternationalNumber() {
        return internationalNumber;
    }

    public void setInternationalNumber(Integer internationalNumber) {
        this.internationalNumber = internationalNumber;
    }

    public String getHorizontalDatum() {
        return horizontalDatum;
    }

    public void setHorizontalDatum(String horizontalDatum) {
        this.horizontalDatum = horizontalDatum;
    }

    public Integer getScale() {
        return scale;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLowerLeftLatitude() {
        return lowerLeftLatitude;
    }

    public void setLowerLeftLatitude(Double lowerLeftLatitude) {
        this.lowerLeftLatitude = lowerLeftLatitude;
    }

    public Double getUpperRightLatitude() {
        return upperRightLatitude;
    }

    public void setUpperRightLatitude(Double upperRightLatitude) {
        this.upperRightLatitude = upperRightLatitude;
    }

    public Double getLowerLeftLongitude() {
        return lowerLeftLongitude;
    }

    public void setLowerLeftLongitude(Double lowerLeftLongitude) {
        this.lowerLeftLongitude = lowerLeftLongitude;
    }

    public Double getUpperRightLongitude() {
        return upperRightLongitude;
    }

    public void setUpperRightLongitude(Double upperRightLongitude) {
        this.upperRightLongitude = upperRightLongitude;
    }
}
