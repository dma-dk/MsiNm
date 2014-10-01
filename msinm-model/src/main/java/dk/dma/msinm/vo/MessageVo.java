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
import dk.dma.msinm.common.model.ILocalizedDesc;
import dk.dma.msinm.common.repo.RepoFileVo;
import dk.dma.msinm.common.vo.LocalizableVo;
import dk.dma.msinm.common.vo.LocalizedDescVo;
import dk.dma.msinm.model.Area;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.model.MessageDesc;
import dk.dma.msinm.model.SeriesIdentifier;
import dk.dma.msinm.model.Status;
import dk.dma.msinm.model.Type;
import org.apache.commons.lang.StringUtils;

import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Value object for the {@code Message} model entity
 */
public class MessageVo extends LocalizableVo<Message, MessageVo.MessageDescVo> {

    Integer id;
    Date created;
    Date updated;
    Integer version;
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
    Boolean originalInformation;
    Boolean bookmarked;
    Boolean firingExercise;
    List<PublicationVo> publications;
    List<RepoFileVo> attachments;

    // Used when creating new messages from the web client
    String repoPath;

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
            created = message.getCreated();
            updated = message.getUpdated();
            version = message.getVersion();
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
            message.getPublications().forEach(publication -> checkCreatePublications().add(new PublicationVo(publication)));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message toEntity() {

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
        if (originalInformation != null) {
            message.setOriginalInformation(originalInformation);
        }
        if (publications != null) {
            publications.forEach(publication -> message.getPublications().add(publication.toEntity(message)));
        }
        if (getDescs() != null) {
            getDescs().stream().forEach(desc -> message.getDescs().add(desc.toEntity(message)));
        }
        return message;
    }

    /**
     * Sorts the descriptive entities to ensure that the given language is first
     * @param lang the language to sort first
     */
    public void sortDescsByLang(final String lang) {
        sortDescs(lang);
        if (area != null) {
            area.sortDescs(lang);
        }
        if (categories != null) {
            categories.forEach(cat -> cat.sortDescs(lang));
        }
        if (locations != null) {
            locations.forEach(loc -> {
                loc.sortDescs(lang);
                if (loc.getPoints() != null) {
                    loc.getPoints().forEach(pt -> pt.sortDescs(lang));
                }
            });
        }
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

    /**
     * Returns or creates the list of publications
     * @return the list of publications
     */
    public List<PublicationVo> checkCreatePublications() {
        if (publications == null) {
            publications = new ArrayList<>();
        }
        return publications;
    }

    /**
     * Adds the given publication if a publication of the same type does not already exists
     * @param publication the type of the publication
     */
    public void addPublicationIfUndefined(PublicationVo publication) {
        if (checkCreatePublications().stream().noneMatch(pub -> pub.getType().equals(publication.getType()))) {
            publications.add(publication);
        }
    }

    /**
     * Returns the publication with the given type, or null if not found
     * @param type the type of the publication
     * @return the publication with the given type
     */
    @Transient
    public PublicationVo getPublication(String type) {
        return publications == null
                ? null
                : publications.stream()
                    .filter(pub -> pub.getType().equals(type))
                    .findFirst()
                    .orElse(null);
    }

    // ************ Getters and setters *************

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
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

    public Boolean isOriginalInformation() {
        return originalInformation;
    }

    public void setOriginalInformation(Boolean originalInformation) {
        this.originalInformation = originalInformation;
    }

    public String getRepoPath() {
        return repoPath;
    }

    public void setRepoPath(String repoPath) {
        this.repoPath = repoPath;
    }

    public Boolean isBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(Boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

    public Boolean getFiringExercise() {
        return firingExercise;
    }

    public void setFiringExercise(Boolean firingExercise) {
        this.firingExercise = firingExercise;
    }

    public List<PublicationVo> getPublications() {
        return publications;
    }

    public void setPublications(List<PublicationVo> publications) {
        this.publications = publications;
    }

    public List<RepoFileVo> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<RepoFileVo> attachments) {
        this.attachments = attachments;
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
         * {@inheritDoc}
         */
        @Override
        public boolean descDefined() {
            return ILocalizedDesc.fieldsDefined(time, description, otherCategories, time, vicinity, note, publication, source);
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


