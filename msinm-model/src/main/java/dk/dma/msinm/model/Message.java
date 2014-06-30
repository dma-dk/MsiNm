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

import dk.dma.msinm.common.model.ILocalizable;
import dk.dma.msinm.common.model.IPreloadable;
import dk.dma.msinm.common.model.VersionedEntity;
import dk.dma.msinm.common.sequence.DefaultSequence;
import dk.dma.msinm.common.sequence.Sequence;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Abstract base class for MSI-NM messages
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NamedQueries({
    @NamedQuery(name="Message.findBySeriesIdentifier",
                query="SELECT msg FROM Message msg inner join msg.seriesIdentifier si where si.number = :number " +
                      " and si.year = :year and si.authority = :authority"),
    @NamedQuery(name="Message.findUpdateMessages",
                query="SELECT msg FROM Message msg where msg.updated > :date order by msg.updated asc"),
    @NamedQuery(name="Message.findActive",
                query="SELECT msg FROM Message msg where msg.status = 'ACTIVE' order by msg.validFrom asc")
})
public class Message extends VersionedEntity<Integer> implements ILocalizable<MessageDesc>, IPreloadable {

    private static final long serialVersionUID = 1L;

    public static final Sequence MESSAGE_SEQUENCE = new DefaultSequence("msinm-message-id", 1);

    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    MessageSeriesIdentifier seriesIdentifier;

    @NotNull
    @Enumerated(EnumType.STRING)
    MessageStatus status;

    @ManyToOne
    Area area;

    @ManyToMany
    List<Category> categories = new ArrayList<>();

    @ManyToMany
    List<Chart> charts = new ArrayList<>();

    String horizontalDatum;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    Date validFrom;

    @Temporal(TemporalType.TIMESTAMP)
    Date validTo;

    @OneToMany(cascade = CascadeType.ALL)
    List<Location> locations = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "entity")
    List<MessageDesc> descs = new ArrayList<>();

    @Temporal(TemporalType.TIMESTAMP)
    Date cancellationDate;

    @OneToMany(cascade = CascadeType.ALL)
    Set<MessageSeriesIdentifier> cancellations = new HashSet<>();

    @NotNull
    Priority priority = Priority.NONE;

    @ElementCollection
    List<String> lightsListNumbers = new ArrayList<>();

    boolean originalInformation;


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
    public void preload() {
        getSeriesIdentifier();
        getLocations().forEach(Location::preload);
        getDescs().forEach(desc -> {});
        getCategories().forEach(cat -> {});
        getCharts().forEach(chart -> {});
        getArea();
        getCancellations().forEach(id -> {});
    }

    /******** Getters and setters *********/

    public MessageSeriesIdentifier getSeriesIdentifier() {
        return seriesIdentifier;
    }

    public void setSeriesIdentifier(MessageSeriesIdentifier seriesIdentifier) {
        this.seriesIdentifier = seriesIdentifier;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
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

    public Set<MessageSeriesIdentifier> getCancellations() {
        return cancellations;
    }

    public void setCancellations(Set<MessageSeriesIdentifier> cancellations) {
        this.cancellations = cancellations;
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

}
