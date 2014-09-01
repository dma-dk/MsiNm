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
package dk.dma.msinm.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import dk.dma.msinm.common.model.DataFilter;
import dk.dma.msinm.common.model.ILocalizedDesc;
import dk.dma.msinm.common.vo.LocalizableVo;
import dk.dma.msinm.common.vo.LocalizedDescVo;
import dk.dma.msinm.model.Location;
import dk.dma.msinm.model.LocationDesc;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Value object for the {@code Location} model entity
 */
public class LocationVo extends LocalizableVo<Location, LocationVo.LocationDescVo> {

    String type;
    Integer radius;
    List<PointVo> points;

    /**
     * Constructor
     */
    public LocationVo() {
        super();
    }

    /**
     * Constructor
     * @param location the location
     */
    public LocationVo(Location location) {
        this(location, DataFilter.get("points"));
    }

    /**
     * Constructor
     * @param location the location
     * @param dataFilter what type of data to include from the entity
     */
    public LocationVo(Location location, DataFilter dataFilter) {
        super(location);

        type = location.getType().toString();
        radius = location.getRadius();
        location.getPoints().forEach(point -> checkCreatePoints().add(new PointVo(point, dataFilter)));
        location.getDescs(dataFilter).stream()
                .forEach(desc -> checkCreateDescs().add(new LocationDescVo(desc)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Location toEntity() {
        Location location = new Location();
        location.setType(Location.LocationType.valueOf(type));
        location.setRadius(radius);
        if (points != null) {
            points.stream()
                    .filter(PointVo::isDefined)
                    .forEach(pt -> location.getPoints().add(pt.toEntity(location)));
        }
        if (getDescs() != null) {
            getDescs().stream()
                    .filter(LocationDescVo::descDefined)
                    .forEach(desc -> location.getDescs().add(desc.toEntity(location)));
        }
        return location;
    }

    /**
     * Returns or creates the list of points
     * @return the list of points
     */
    public List<PointVo> checkCreatePoints() {
        if (points == null) {
            points = new ArrayList<>();
        }
        return points;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getRadius() {
        return radius;
    }

    public void setRadius(Integer radius) {
        this.radius = radius;
    }

    public List<PointVo> getPoints() {
        return points;
    }

    @JsonDeserialize(contentAs = PointVo.class)
    public void setPoints(List<PointVo> points) {
        this.points = points;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocationDescVo createDesc(String lang) {
        LocationDescVo desc = new LocationDescVo();
        checkCreateDescs().add(desc);
        desc.setLang(lang);
        return desc;
    }

    /**
     * The entity description VO
     */
    public static class LocationDescVo extends LocalizedDescVo<LocationDesc, LocationVo> {

        String description;

        /**
         * Constructor
         */
        public LocationDescVo() {
            super();
        }

        /**
         * Constructor
         * @param desc the entity
         */
        public LocationDescVo(LocationDesc desc) {
            super(desc);
            description = desc.getDescription();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public LocationDesc toEntity() {
            LocationDesc desc = new LocationDesc();
            desc.setLang(getLang());
            desc.setDescription(StringUtils.trim(description));
            return desc;
        }

        public LocationDesc toEntity(Location location) {
            LocationDesc desc = toEntity();
            desc.setEntity(location);
            return desc;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean descDefined() {
            return ILocalizedDesc.fieldsDefined(description);
        }
    }

}
