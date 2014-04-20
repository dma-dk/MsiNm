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
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Abstract base class for MSI-NM messages
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NamedQueries({
    @NamedQuery(name="Message.findBySeriesIdentifier",
                query="SELECT msg FROM Message msg where msg.seriesIdentifier.number = :number " +
                      " and msg.seriesIdentifier.year = :year and msg.seriesIdentifier.authority = :authority")
}) 
public abstract class Message extends BaseEntity<Integer> {

    private static final long serialVersionUID = 1L;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    private MessageSeriesIdentifier seriesIdentifier;
    
    @NotNull
    private String generalArea;
    
    @NotNull
    private String locality;
    
    @ElementCollection
    private List<String> specificLocation = new ArrayList<>();
    
    @ElementCollection
    private List<String> chartNumber = new ArrayList<>();
    
    @ElementCollection
    private List<Integer> intChartNumber = new ArrayList<>();
    
    @NotNull
    private Date issueDate;

    /**
     * Constructor
     */
    public Message() {
    }
    
    /******** Getters and setters *********/
    
    public MessageSeriesIdentifier getSeriesIdentifier() {
        return seriesIdentifier;
    }
    
    public void setSeriesIdentifier(MessageSeriesIdentifier seriesIdentifier) {
        this.seriesIdentifier = seriesIdentifier;
    }

    public String getGeneralArea() {
        return generalArea;
    }

    public void setGeneralArea(String generalArea) {
        this.generalArea = generalArea;
    }
    
    public String getLocality() {
        return locality;
    }
    
    public void setLocality(String locality) {
        this.locality = locality;
    }

    public List<String> getSpecificLocation() {
        return specificLocation;
    }
    
    public void setSpecificLocation(List<String> specificLocation) {
        this.specificLocation = specificLocation;
    }
    
    public List<String> getChartNumber() {
        return chartNumber;
    }
    
    public void setChartNumber(List<String> chartNumber) {
        this.chartNumber = chartNumber;
    }
    
    public List<Integer> getIntChartNumber() {
        return intChartNumber;
    }
    
    public void setIntChartNumber(List<Integer> intChartNumber) {
        this.intChartNumber = intChartNumber;
    }
    
    public Date getIssueDate() {
        return issueDate;
    }
    
    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }
    
}
