package dk.dma.msinm.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import dk.dma.msinm.common.vo.LocalizableVo;
import dk.dma.msinm.common.vo.LocalizedDescVo;
import dk.dma.msinm.model.Area;
import dk.dma.msinm.model.AreaDesc;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Value object for the {@code Area} model entity
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class AreaVo extends LocalizableVo<Area, AreaVo.AreaDescVo> {
    Integer id;
    Integer parentId;
    List<LocationVo> locations = new ArrayList<>();
    List<AreaVo> childAreas = new ArrayList<>();

    /**
     * Constructor
     */
    public AreaVo() {
    }

    /**
     * Constructor
     * @param area the area
     */
    public AreaVo(Area area) {
        super(area);

        id = area.getId();
        area.getLocations().forEach(loc -> locations.add(new LocationVo(loc)));
        area.getChildAreas().forEach(childArea -> childAreas.add(new AreaVo(childArea)));
        area.getDescs().forEach(desc -> getDescs().add(new AreaDescVo(desc)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Area toEntity() {
        Area area = new Area();
        area.setId(id);
        locations.stream()
                .filter(loc -> loc.getPoints().size() > 0)
                .forEach(loc -> area.getLocations().add(loc.toEntity()));
        getDescs().stream()
                .filter(desc -> StringUtils.isNotBlank(desc.getName()))
                .forEach(desc -> area.getDescs().add(desc.toEntity(area)));
        return area;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public List<LocationVo> getLocations() {
        return locations;
    }

    @JsonDeserialize(contentAs = LocationVo.class)
    public void setLocations(List<LocationVo> locations) {
        this.locations = locations;
    }

    public List<AreaVo> getChildAreas() {
        return childAreas;
    }

    public void setChildAreas(List<AreaVo> childAreas) {
        this.childAreas = childAreas;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AreaDescVo createDesc(String lang) {
        AreaDescVo area = new AreaDescVo();
        area.setLang(lang);
        return area;
    }

    /**
     * The entity description VO
     */
    public static class AreaDescVo extends LocalizedDescVo<AreaDesc, AreaVo> {

        String name;

        /**
         * Constructor
         */
        public AreaDescVo() {
            super();
        }

        /**
         * Constructor
         * @param desc the entity
         */
        public AreaDescVo(AreaDesc desc) {
            super(desc);
            name = desc.getName();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public AreaDesc toEntity() {
            AreaDesc desc = new AreaDesc();
            desc.setLang(getLang());
            desc.setName(name);
            return desc;
        }

        public AreaDesc toEntity(Area area) {
            AreaDesc desc = toEntity();
            desc.setEntity(area);
            return desc;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
