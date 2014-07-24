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
public class AreaVo extends LocalizableVo<Area, AreaVo.AreaDescVo> {
    Integer id;
    AreaVo parent;
    List<LocationVo> locations;
    List<AreaVo> children;

    /**
     * Constructor
     */
    public AreaVo() {
    }

    /**
     * Constructor
     *
     * @param area the area
     * @param dataFilter what type of data to include from the entity
     */
    public AreaVo(Area area, DataFilter dataFilter) {
        super(area);

        DataFilter compFilter = dataFilter.forComponent(Area.class);

        id = area.getId();

        if (compFilter.includeLocations()) {
            area.getLocations().forEach(loc -> checkCreateLocations().add(new LocationVo(loc, compFilter)));
        }

        if (compFilter.includeChildren()) {
            area.getChildren().forEach(child -> checkCreateChildren().add(new AreaVo(child, compFilter)));
        }

        if (compFilter.includeParent() && area.getParent() != null) {
            parent = new AreaVo(area.getParent(), compFilter);
        } else if (compFilter.includeParentId() && area.getParent() != null) {
            parent = new AreaVo();
            parent.setId(area.getParent().getId());
        }

        area.getDescs().stream()
            .filter(compFilter::includeLang)
            .forEach(desc -> checkCreateDescs().add(new AreaDescVo(desc)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Area toEntity() {
        Area area = new Area();
        area.setId(id);
        if (locations != null) {
            locations.stream()
                    .filter(loc -> loc.getPoints().size() > 0)
                    .forEach(loc -> area.getLocations().add(loc.toEntity()));
        }
        if (getDescs() != null) {
            getDescs().stream()
                    .filter(desc -> StringUtils.isNotBlank(desc.getName()))
                    .forEach(desc -> area.getDescs().add(desc.toEntity(area)));
        }

        return area;
    }

    /**
     * Returns or creates the list of locations
     * @return the list of locations
     */
    public List<LocationVo> checkCreateLocations() {
        if (locations == null) {
            locations = new ArrayList<>();
        }
        return locations;
    }

    /**
     * Returns or creates the list of child areas
     * @return the list of child areas
     */
    public List<AreaVo> checkCreateChildren() {
        if (children == null) {
            children = new ArrayList<>();
        }
        return children;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public AreaVo getParent() {
        return parent;
    }

    public void setParent(AreaVo parent) {
        this.parent = parent;
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
        AreaDescVo desc = new AreaDescVo();
        desc.setLang(lang);
        checkCreateDescs().add(desc);
        return desc;
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
            desc.setName(StringUtils.trim(name));
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
