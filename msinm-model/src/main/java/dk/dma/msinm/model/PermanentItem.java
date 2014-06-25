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
 * Contains information for permanent NtM messages 
 */
@Entity
public class PermanentItem extends BaseEntity<Integer> {

    private static final long serialVersionUID = 1L;

    @Column(length = 2000, nullable = false)
    private String amplifyingRemarks;
    
    private String chartNumber;
    
    private String horizontalDatum;
    
    @OneToOne
    private MessageSeriesIdentifier lastUpdate;
    
    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    private Location location;

    @OneToMany(cascade = CascadeType.ALL)
    private List<NoticeElement> noticeElements = new ArrayList<>();

    public PermanentItem() {

    }

    public String getAmplifyingRemarks() {
        return amplifyingRemarks;
    }

    public void setAmplifyingRemarks(String amplifyingRemarks) {
        this.amplifyingRemarks = amplifyingRemarks;
    }

    public String getChartNumber() {
        return chartNumber;
    }

    public void setChartNumber(String chartNumber) {
        this.chartNumber = chartNumber;
    }

    public String getHorizontalDatum() {
        return horizontalDatum;
    }

    public void setHorizontalDatum(String horizontalDatum) {
        this.horizontalDatum = horizontalDatum;
    }

    public MessageSeriesIdentifier getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(MessageSeriesIdentifier lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
    
    public List<NoticeElement> getNoticeElements() {
        return noticeElements;
    }
    
    public void setNoticeElements(List<NoticeElement> noticeElements) {
        this.noticeElements = noticeElements;
    }

}
