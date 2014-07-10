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
    List<PointVo> points = new ArrayList<>();

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
        this(location, CopyOp.get("points"));
    }

    /**
     * Constructor
     * @param location the location
     * @param copyOp what type of data to copy from the entity
     */
    public LocationVo(Location location, CopyOp copyOp) {
        super(location);

        type = location.getType().toString();
        radius = location.getRadius();
        location.getPoints().forEach(point -> points.add(new PointVo(point, copyOp)));
        location.getDescs().stream()
                .filter(copyOp::copyLang)
                .forEach(desc -> getDescs().add(new LocationDescVo(desc)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Location toEntity() {
        Location location = new Location();
        location.setType(Location.LocationType.valueOf(type));
        location.setRadius(radius);
        points.stream()
                .filter(PointVo::isDefined)
                .forEach(pt -> location.getPoints().add(pt.toEntity(location)));
        getDescs().stream()
                .filter(desc -> StringUtils.isNotBlank(desc.getDescription()))
                .forEach(desc -> location.getDescs().add(desc.toEntity(location)));
        return location;
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
        getDescs().add(desc);
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
            desc.setDescription(description);
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
    }

}
