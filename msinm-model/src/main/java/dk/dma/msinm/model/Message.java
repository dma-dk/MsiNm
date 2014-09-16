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
package dk.dma.msinm.model;

import dk.dma.msinm.common.model.DataFilter;
import dk.dma.msinm.common.model.ILocalizable;
import dk.dma.msinm.common.model.IPreloadable;
import dk.dma.msinm.common.model.VersionedEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * Abstract base class for MSI-NM messages
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NamedQueries({
    @NamedQuery(name="Message.findBySeriesIdentifier",
                query="SELECT msg FROM Message msg where msg.seriesIdentifier.mainType = :type and msg.seriesIdentifier.number = :number " +
                      " and msg.seriesIdentifier.year = :year and msg.seriesIdentifier.authority = :authority"),
    @NamedQuery(name="Message.findUpdateMessages",
                query="SELECT msg FROM Message msg where msg.updated > :date order by msg.updated asc"),
    @NamedQuery(name="Message.findActive",
                query="SELECT msg FROM Message msg where msg.status = 'PUBLISHED' order by msg.validFrom asc"),
    @NamedQuery(name="Message.findActiveNotices",
                query="SELECT msg FROM Message msg where msg.status = 'PUBLISHED' and msg.seriesIdentifier.mainType = 'NM'" +
                      " and msg.validFrom < :date and (msg.validTo is null or msg.validTo > :date)"),
    @NamedQuery(name="Message.findPublishedExpiredMessages",
                query="SELECT msg FROM Message msg where msg.status = 'PUBLISHED' and msg.validTo is not null and msg.validTo < CURRENT_TIMESTAMP "),
    @NamedQuery(name="Message.findActiveByCategory",
                query="SELECT distinct msg FROM Message msg left join msg.categories c where msg.status = 'PUBLISHED' and c.lineage like :lineage order by msg.validFrom asc"),
})
public class Message extends VersionedEntity<Integer> implements ILocalizable<MessageDesc>, IPreloadable {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Embedded
    SeriesIdentifier seriesIdentifier;

    @NotNull
    @Enumerated(EnumType.STRING)
    Type type;

    @NotNull
    @Enumerated(EnumType.STRING)
    Status status;

    @ManyToOne(cascade = CascadeType.ALL)
    Area area;

    @ManyToMany(cascade = CascadeType.ALL)
    List<Category> categories = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.ALL)
    @OrderBy("coalesce(scale, 99999999) ASC")
    List<Chart> charts = new ArrayList<>();

    String horizontalDatum;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    Date validFrom;

    @Temporal(TemporalType.TIMESTAMP)
    Date validTo;

    @OneToMany(cascade = CascadeType.ALL)
    List<Location> locations = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "entity", orphanRemoval = true)
    List<MessageDesc> descs = new ArrayList<>();

    @Temporal(TemporalType.TIMESTAMP)
    Date cancellationDate;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "message", orphanRemoval = true)
    Set<Reference> references = new HashSet<>();

    @NotNull
    Priority priority = Priority.NONE;

    @ElementCollection
    List<String> lightsListNumbers = new ArrayList<>();

    boolean originalInformation;

    @OneToMany(cascade = CascadeType.ALL)
    List<Publication> publications = new ArrayList<>();

    /**
     * Constructor
     */
    public Message() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageDesc createDesc(String lang) {
        MessageDesc desc = new MessageDesc();
        desc.setLang(lang);
        desc.setEntity(this);
        getDescs().add(desc);
        return desc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preload(DataFilter dataFilter) {
        DataFilter compFilter = dataFilter.forComponent(Message.class);

        getSeriesIdentifier();

        if (compFilter.includeAnyOf("details", DataFilter.LOCATIONS)) {
            getLocations().forEach(Location::preload);
        }
        if (compFilter.include("details")) {
            getCharts().forEach(chart -> {});
            getReferences().forEach(id -> {});
            getLightsListNumbers().forEach(light -> {});
            getCategories().forEach(cat -> cat.preload(compFilter));
            if (getArea() != null) {
                getArea().preload(compFilter);
            }
            getPublications().forEach(publication -> {});
        }
        getDescs().forEach(desc -> {});
    }

    /**
     * Returns the publication with the given type, or null if not found
     * @param type the type of the publication
     * @return the publication with the given type
     */
    @Transient
    public Publication getPublication(String type) {
        return getPublications().stream()
                .filter(pub -> pub.getType().equals(type))
                .findFirst()
                .orElse(null);
    }

    /******** Getters and setters *********/

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

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public List<Chart> getCharts() {
        return charts;
    }

    public void setCharts(List<Chart> charts) {
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

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    public Date getCancellationDate() {
        return cancellationDate;
    }

    public void setCancellationDate(Date cancellationDate) {
        this.cancellationDate = cancellationDate;
    }

    public Set<Reference> getReferences() {
        return references;
    }

    public void setReferences(Set<Reference> references) {
        this.references = references;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
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

    @Override
    public List<MessageDesc> getDescs() {
        return descs;
    }

    @Override
    public void setDescs(List<MessageDesc> descs) {
        this.descs = descs;
    }

    public List<Publication> getPublications() {
        return publications;
    }

    public void setPublications(List<Publication> publications) {
        this.publications = publications;
    }
}
