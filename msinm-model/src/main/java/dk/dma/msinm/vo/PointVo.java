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

import dk.dma.msinm.common.vo.LocalizableVo;
import dk.dma.msinm.common.vo.LocalizedDescVo;
import dk.dma.msinm.model.Location;
import dk.dma.msinm.model.Point;
import dk.dma.msinm.model.PointDesc;
import org.apache.commons.lang.StringUtils;

/**
 * Value object for the {@code Point} model entity
 */
public class PointVo extends LocalizableVo<Point, PointVo.PointDescVo> {
    Double lat;
    Double lon;
    int index;

    /**
     * Constructor
     */
    public PointVo() {
        super();
    }

    /**
     * Constructor
     * @param point the point
     */
    public PointVo(Point point) {
        this(point, new CopyOp());
    }

    /**
     * Constructor
     * @param point the point
     * @param copyOp what type of data to copy from the entity
     */
    public PointVo(Point point, CopyOp copyOp) {
        super(point);

        lat = point.getLat();
        lon = point.getLon();
        index = point.getIndex();
        point.getDescs().stream()
            .filter(copyOp::copyLang)
            .forEach(desc -> checkCreateDescs().add(new PointDescVo(desc)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Point toEntity() {
        Point point = new Point();
        point.setLat(lat);
        point.setLon(lon);
        point.setIndex(index);
        if (getDescs() != null) {
            getDescs().stream()
                    .filter(desc -> StringUtils.isNotBlank(desc.getDescription()))
                    .forEach(desc -> point.getDescs().add(desc.toEntity(point)));
        }
        return point;
    }

    /**
     * Converts this VO to a Point
     * @param location the location
     * @return the point
     */
    public Point toEntity(Location location) {
        Point point = toEntity();
        point.setLocation(location);
        return point;
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

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isDefined() {
        return lat != null && lon != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PointDescVo createDesc(String lang) {
        PointDescVo desc = new PointDescVo();
        checkCreateDescs().add(desc);
        desc.setLang(lang);
        return desc;
    }

    /**
     * The entity description VO
     */
    public static class PointDescVo extends LocalizedDescVo<PointDesc, PointVo> {

        String description;

        /**
         * Constructor
         */
        public PointDescVo() {
            super();
        }

        /**
         * Constructor
         * @param desc the entity
         */
        public PointDescVo(PointDesc desc) {
            super(desc);
            description = desc.getDescription();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public PointDesc toEntity() {
            PointDesc desc = new PointDesc();
            desc.setLang(getLang());
            desc.setDescription(StringUtils.trim(description));
            return desc;
        }

        public PointDesc toEntity(Point point) {
            PointDesc desc = toEntity();
            desc.setEntity(point);
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
