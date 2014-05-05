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
import java.util.ArrayList;
import java.util.List;

/**
 * Contains information for temporary (T) and preliminary (P) NtM messages 
 */
@Entity
public class TempPreliminaryItem extends BaseEntity<Integer> {

    private static final long serialVersionUID = 1L;

    @ElementCollection
    private List<String> graphicalRepresentations = new ArrayList<>();

    @Column(length = 2000, nullable = false)
    private String itemDescription;
    
    @OneToOne(cascade = CascadeType.ALL)
    private MessageLocation location;

    public TempPreliminaryItem() {

    }

    public List<String> getGraphicalRepresentations() {
        return graphicalRepresentations;
    }

    public void setGraphicalRepresentations(List<String> graphicalRepresentations) {
        this.graphicalRepresentations = graphicalRepresentations;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public MessageLocation getLocation() {
        return location;
    }

    public void setLocation(MessageLocation location) {
        this.location = location;
    }

}
