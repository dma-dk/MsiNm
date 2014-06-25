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

import dk.dma.msinm.common.model.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Used in {@code PermanentItem}s to describe a chart update operation
 */
@Entity
public class NoticeElement extends BaseEntity<Integer> {

    private static final long serialVersionUID = 1L;

    @Column(length = 2000, nullable = false)
    private String amplifyingNote;
    
    @NotNull
    private String featureOrCharacteristic;
    
    @ElementCollection
    private List<String> graphicalRepresentations = new ArrayList<>();
    
    @NotNull
    @Enumerated(EnumType.STRING)
    private NoticeVerb noticeVerb;

    @OneToOne(cascade = CascadeType.ALL)
    private Location location;

    public NoticeElement() {

    }

    public String getAmplifyingNote() {
        return amplifyingNote;
    }

    public void setAmplifyingNote(String amplifyingNote) {
        this.amplifyingNote = amplifyingNote;
    }

    public String getFeatureOrCharacteristic() {
        return featureOrCharacteristic;
    }

    public void setFeatureOrCharacteristic(String featureOrCharacteristic) {
        this.featureOrCharacteristic = featureOrCharacteristic;
    }

    public List<String> getGraphicalRepresentations() {
        return graphicalRepresentations;
    }

    public void setGraphicalRepresentations(List<String> graphicalRepresentations) {
        this.graphicalRepresentations = graphicalRepresentations;
    }

    public NoticeVerb getNoticeVerb() {
        return noticeVerb;
    }

    public void setNoticeVerb(NoticeVerb noticeVerb) {
        this.noticeVerb = noticeVerb;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

}
