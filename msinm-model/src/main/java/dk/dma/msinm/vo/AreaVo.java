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
    List<AreaVo> children = new ArrayList<>();

    /**
     * Constructor
     */
    public AreaVo() {
    }

    /**
     * Constructor
     * @param area the area
     * @param includeChildren whether to include child areas or not
     */
    public AreaVo(Area area, boolean includeChildren) {
        super(area);

        id = area.getId();
        area.getLocations().forEach(loc -> locations.add(new LocationVo(loc)));
        area.getDescs().forEach(desc -> getDescs().add(new AreaDescVo(desc)));
        if (includeChildren) {
            area.getChildren().forEach(child -> children.add(new AreaVo(child)));
        }
    }

    /**
     * Constructor
     * @param area the area
     */
    public AreaVo(Area area) {
        this(area, true);
    }

    /**
     * Constructor
     *
     * This version only reads the description records with given language,
     * and discards locations and child areas
     *
     * @param area the area
     */
    public AreaVo(Area area, String lang) {
        super(area);

        id = area.getId();
        parentId = (area.getParent() == null) ? null : area.getParent().getId();
        area.getDescs().stream()
            .filter(desc -> desc.getLang().equals(lang))
            .forEach(desc -> getDescs().add(new AreaDescVo(desc)));
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

    public List<AreaVo> getChildren() {
        return children;
    }

    public void setChildren(List<AreaVo> children) {
        this.children = children;
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
