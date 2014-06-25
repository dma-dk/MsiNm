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
        super(point);

        lat = point.getLat();
        lon = point.getLon();
        index = point.getIndex();
        point.getDescs().forEach(desc -> getDescs().add(new PointDescVo(desc)));
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
        getDescs().stream()
                .filter(desc -> StringUtils.isNotBlank(desc.getDescription()))
                .forEach(desc -> point.getDescs().add(desc.toEntity(point)));
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
        PointDescVo point = new PointDescVo();
        point.setLang(lang);
        return point;
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
            desc.setDescription(description);
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
