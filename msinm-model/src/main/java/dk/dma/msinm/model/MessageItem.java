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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains information for MSI navigational warnings
 */
@Entity
public class MessageItem extends BaseEntity<Integer> {

    private static final long serialVersionUID = 1L;

    @NotNull
    private String keySubject;
    
    private String amplifyingRemarks;
    
    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    private MessageCategory category;
    
    @OneToMany(cascade = CascadeType.ALL)
    private List<MessageLocation> location = new ArrayList<>();
    
    @ManyToOne
    private NavwarnMessage navwarnMessage;

    public MessageItem() {
    }

    public String getAmplifyingRemarks() {
        return amplifyingRemarks;
    }

    public void setAmplifyingRemarks(String amplifyingRemarks) {
        this.amplifyingRemarks = amplifyingRemarks;
    }

    public String getKeySubject() {
        return keySubject;
    }

    public void setKeySubject(String keySubject) {
        this.keySubject = keySubject;
    }

    public MessageCategory getCategory() {
        return category;
    }

    public void setCategory(MessageCategory category) {
        this.category = category;
    }
    
    public List<MessageLocation> getLocation() {
        return location;
    }
    
    public void setLocation(List<MessageLocation> location) {
        this.location = location;
    }

    public NavwarnMessage getNavwarnMessage() {
        return navwarnMessage;
    }

    public void setNavwarnMessage(NavwarnMessage navwarnMessage) {
        this.navwarnMessage = navwarnMessage;
    }

}
