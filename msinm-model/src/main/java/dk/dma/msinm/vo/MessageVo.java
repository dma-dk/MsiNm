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

import dk.dma.msinm.common.model.DataFilter;
import dk.dma.msinm.common.vo.LocalizableVo;
import dk.dma.msinm.common.vo.LocalizedDescVo;
import dk.dma.msinm.model.*;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * Value object for the {@code Message} model entity
 */
public class MessageVo extends LocalizableVo<Message, MessageVo.MessageDescVo> {

    Integer id;
    SeriesIdentifier seriesIdentifier;
    Type type;
    Status status;
    AreaVo area;
    List<CategoryVo> categories;
    List<LocationVo> locations;
    List<ChartVo> charts;
    String horizontalDatum;
    Date validFrom;
    Date validTo;
    Date cancellationDate;
    Set<ReferenceVo> references;
    List<String> lightsListNumbers;
    boolean originalInformation;


    /**
     * Constructor
     */
    public MessageVo() {
    }

    /**
     * Constructor
     * @param message the message
     * @param dataFilter what type of data to include from the entity
     */
    public MessageVo(Message message, DataFilter dataFilter) {
        super(message);

        DataFilter compFilter = dataFilter.forComponent(Message.class);

        id = message.getId();

        seriesIdentifier = message.getSeriesIdentifier();
        type = message.getType();
        validFrom = message.getValidFrom();
        validTo = message.getValidTo();

        if (compFilter.includeAnyOf("details", "MessageDesc.title")) {
            message.getDescs(compFilter).stream()
                    .forEach(desc -> checkCreateDescs().add(new MessageDescVo(desc, compFilter)));
        }

        if (compFilter.includeAnyOf("details", "locations")) {
            message.getLocations().forEach(loc -> checkCreateLocations().add(new LocationVo(loc, compFilter)));
        }

        if (compFilter.include("details")) {
            area = (message.getArea() == null) ? null : new AreaVo(message.getArea(), compFilter);
            status = message.getStatus();
            message.getCategories().forEach(cat -> checkCreateCategories().add(new CategoryVo(cat, compFilter)));
            message.getCharts().forEach(chart -> checkCreateCharts().add(new ChartVo(chart)));
            horizontalDatum = message.getHorizontalDatum();
            cancellationDate = message.getCancellationDate();
            message.getReferences().forEach(ref -> checkCreateReferences().add(new ReferenceVo(ref)));
            if (message.getLightsListNumbers().size() > 0) {
                checkCreateLightsListNumbers().addAll(message.getLightsListNumbers());
            }
            originalInformation = message.isOriginalInformation();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message toEntity() {
        // TODO: Update ORM references

        Message message = new Message();
        message.setId(id);
        message.setSeriesIdentifier(seriesIdentifier);
        message.setStatus(status);
        message.setType(type);
        Area msgArea = null;
        for (AreaVo areaVo = area; areaVo != null; areaVo = areaVo.getParent()) {
            Area childArea = msgArea;
            msgArea = areaVo.toEntity();
            if (childArea != null) {
                childArea.setParent(msgArea);
                msgArea.getChildren().add(childArea);
            } else {
                message.setArea(msgArea);
            }
        }
        if (categories != null) {
            categories.forEach(cat -> message.getCategories().add(cat.toEntity()));
        }
        if (locations != null) {
            locations.stream()
                    .filter(loc -> loc.getPoints().size() > 0)
                    .forEach(loc -> message.getLocations().add(loc.toEntity()));
        }
        if (charts != null) {
            charts.forEach(chart -> message.getCharts().add(chart.toEntity()));
        }
        message.setHorizontalDatum(horizontalDatum);
        message.setValidFrom(validFrom);
        message.setValidTo(validTo);
        message.setCancellationDate(cancellationDate);
        if (references != null) {
            references.forEach(ref -> message.getReferences().add(ref.toEntity(message)));
        }
        if (lightsListNumbers != null) {
            message.getLightsListNumbers().addAll(lightsListNumbers);
        }
        message.setOriginalInformation(originalInformation);
        if (getDescs() != null) {
            getDescs().stream()
                    .filter(MessageDescVo::isDefined)
                    .forEach(desc -> message.getDescs().add(desc.toEntity(message)));
        }
        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageDescVo createDesc(String lang) {
        MessageDescVo desc = new MessageDescVo();
        checkCreateDescs().add(desc);
        desc.setLang(lang);
        return desc;
    }

    /**
     * Returns or creates the list of categories
     * @return the list of categories
     */
    public List<CategoryVo> checkCreateCategories() {
        if (categories == null) {
            categories = new ArrayList<>();
        }
        return categories;
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
     * Returns or creates the list of charts
     * @return the list of charts
     */
    public List<ChartVo> checkCreateCharts() {
        if (charts == null) {
            charts = new ArrayList<>();
        }
        return charts;
    }

    /**
     * Returns or creates the list of references
     * @return the list of references
     */
    public Set<ReferenceVo> checkCreateReferences() {
        if (references == null) {
            references = new HashSet<>();
        }
        return references;
    }

    /**
     * Returns or creates the list of light numbers
     * @return the list of light numbers
     */
    public List<String> checkCreateLightsListNumbers() {
        if (lightsListNumbers == null) {
            lightsListNumbers = new ArrayList<>();
        }
        return lightsListNumbers;
    }

    // ************ Getters and setters *************

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public SeriesIdentifier getSeriesIdentifier() {
        return seriesIdentifier;
    }

    public void setSeriesIdentifier(SeriesIdentifier seriesIdentifier) {
        this.seriesIdentifier = seriesIdentifier;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public AreaVo getArea() {
        return area;
    }

    public void setArea(AreaVo area) {
        this.area = area;
    }

    public List<CategoryVo> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryVo> categories) {
        this.categories = categories;
    }

    public List<LocationVo> getLocations() {
        return locations;
    }

    public void setLocations(List<LocationVo> locations) {
        this.locations = locations;
    }

    public List<ChartVo> getCharts() {
        return charts;
    }

    public void setCharts(List<ChartVo> charts) {
        this.charts = charts;
    }

    public String getHorizontalDatum() {
        return horizontalDatum;
    }

    public void setHorizontalDatum(String horizontalDatum) {
        this.horizontalDatum = horizontalDatum;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public Date getValidTo() {
        return validTo;
    }

    public void setValidTo(Date validTo) {
        this.validTo = validTo;
    }

    public Date getCancellationDate() {
        return cancellationDate;
    }

    public void setCancellationDate(Date cancellationDate) {
        this.cancellationDate = cancellationDate;
    }

    public Set<ReferenceVo> getReferences() {
        return references;
    }

    public void setReferences(Set<ReferenceVo> references) {
        this.references = references;
    }

    public List<String> getLightsListNumbers() {
        return lightsListNumbers;
    }

    public void setLightsListNumbers(List<String> lightsListNumbers) {
        this.lightsListNumbers = lightsListNumbers;
    }

    public boolean isOriginalInformation() {
        return originalInformation;
    }

    public void setOriginalInformation(boolean originalInformation) {
        this.originalInformation = originalInformation;
    }

    /**
     * The entity description VO
     */
    public static class MessageDescVo extends LocalizedDescVo<MessageDesc, MessageVo> {

        String title;
        String description;
        String otherCategories;
        String time;
        String vicinity;
        String note;
        String publication;
        String source;

        /**
         * Constructor
         */
        public MessageDescVo() {
            super();
        }

        /**
         * Constructor
         * @param desc the entity
         * @param dataFilter what type of data to include from the entity
         */
        public MessageDescVo(MessageDesc desc, DataFilter dataFilter) {
            super(desc);

            DataFilter compFilter = dataFilter.forComponent(MessageDesc.class);

            if (compFilter.include("title")) {
                this.title = desc.getTitle();
            } else {
                this.title = desc.getTitle();
                this.description = desc.getDescription();
                this.otherCategories = desc.getOtherCategories();
                this.time = desc.getTime();
                this.vicinity = desc.getVicinity();
                this.note = desc.getNote();
                this.publication = desc.getPublication();
                this.source = desc.getSource();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public MessageDesc toEntity() {
            MessageDesc desc = new MessageDesc();
            desc.setLang(getLang());
            desc.setTitle(StringUtils.trim(title));
            desc.setDescription(StringUtils.trim(description));
            desc.setOtherCategories(StringUtils.trim(otherCategories));
            desc.setTime(StringUtils.trim(time));
            desc.setVicinity(StringUtils.trim(vicinity));
            desc.setNote(StringUtils.trim(note));
            desc.setPublication(StringUtils.trim(publication));
            desc.setSource(StringUtils.trim(source));
            return desc;
        }

        public MessageDesc toEntity(Message message) {
            MessageDesc desc = toEntity();
            desc.setEntity(message);
            return desc;
        }

        /**
         * Checks that this is a non-blank welldefined description entity
         * @return if this is welldefined
         */
        public boolean isDefined() {
            return StringUtils.isNotBlank(title) ||
                    StringUtils.isNotBlank(description) ||
                    StringUtils.isNotBlank(otherCategories) ||
                    StringUtils.isNotBlank(time) ||
                    StringUtils.isNotBlank(vicinity) ||
                    StringUtils.isNotBlank(note) ||
                    StringUtils.isNotBlank(publication) ||
                    StringUtils.isNotBlank(source);
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getOtherCategories() {
            return otherCategories;
        }

        public void setOtherCategories(String otherCategories) {
            this.otherCategories = otherCategories;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getVicinity() {
            return vicinity;
        }

        public void setVicinity(String vicinity) {
            this.vicinity = vicinity;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public String getPublication() {
            return publication;
        }

        public void setPublication(String publication) {
            this.publication = publication;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }
    }

}


