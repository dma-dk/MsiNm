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
    String description;
    int radius;
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
        super(location);

        type = location.getType().toString();
        radius = location.getRadius();
        location.getPoints().forEach(point -> points.add(new PointVo(point)));
        location.getDescs().forEach(desc -> getDescs().add(new LocationDescVo(desc)));
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
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
        LocationDescVo location = new LocationDescVo();
        location.setLang(lang);
        return location;
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
