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

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

/**
 * An NtM specialization of the {@code Message} class.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class NoticeMessage extends Message {

    private static final long serialVersionUID = 1L;

    @ElementCollection
    private List<String> lightsListNumbers = new ArrayList<>();
    
    private String authority;
    
    private String amplifyingRemarks;
    
    @OneToMany(cascade = CascadeType.ALL)
    private List<PermanentItem> permanentItems = new ArrayList<>();
    
    @OneToMany(cascade = CascadeType.ALL)
    private List<TempPreliminaryItem> tempPreliminaryItems = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    private List<MessageCategory> categories = new ArrayList<>();

    
    public NoticeMessage() {

    }

    public List<String> getLightsListNumbers() {
        return lightsListNumbers;
    }

    public void setLightsListNumbers(List<String> lightsListNumbers) {
        this.lightsListNumbers = lightsListNumbers;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public String getAmplifyingRemarks() {
        return amplifyingRemarks;
    }

    public void setAmplifyingRemarks(String amplifyingRemarks) {
        this.amplifyingRemarks = amplifyingRemarks;
    }
    
    public List<PermanentItem> getPermanentItems() {
        return permanentItems;
    }
    
    public void setPermanentItems(List<PermanentItem> permanentItems) {
        this.permanentItems = permanentItems;
    }
    
    public List<TempPreliminaryItem> getTempPreliminaryItems() {
        return tempPreliminaryItems;
    }
    
    public void setTempPreliminaryItems(List<TempPreliminaryItem> tempPreliminaryItems) {
        this.tempPreliminaryItems = tempPreliminaryItems;
    }

    public List<MessageCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<MessageCategory> categories) {
        this.categories = categories;
    }
}
