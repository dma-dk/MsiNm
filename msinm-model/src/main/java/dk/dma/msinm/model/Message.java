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

import dk.dma.msinm.common.model.VersionedEntity;
import dk.dma.msinm.common.sequence.DefaultSequence;
import dk.dma.msinm.common.sequence.Sequence;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.persistence.*;
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
                query="SELECT msg FROM Message msg inner join msg.seriesIdentifier si where si.number = :number " +
                      " and si.year = :year and si = :authority"),
    @NamedQuery(name="Message.findUpdateMessages",
            query="SELECT msg FROM Message msg where msg.updated > :date order by msg.updated asc"),
})
public abstract class Message extends VersionedEntity<Integer> {

    private static final long serialVersionUID = 1L;
    public static final Sequence MESSAGE_SEQUENCE = new DefaultSequence("msinm-message-id", 1);

    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    private MessageSeriesIdentifier seriesIdentifier;
    
    @NotNull
    private String generalArea;
    
    @NotNull
    private String locality;
    
    @ElementCollection
    private List<String> specificLocations = new ArrayList<>();
    
    @ElementCollection
    private List<String> chartNumbers = new ArrayList<>();
    
    @ElementCollection
    private List<Integer> intChartNumbers = new ArrayList<>();
    
    @NotNull
    private Date issueDate;

    /**
     * Constructor
     */
    public Message() {
    }

    /**
     * Creates a Json representation of this entity
     * @return the Json representation
     */
    public JsonObjectBuilder toJson() {
        JsonArrayBuilder specificLocationsJson = Json.createArrayBuilder();
        specificLocations.forEach(specificLocationsJson::add);
        JsonArrayBuilder chartNumbersJson = Json.createArrayBuilder();
        chartNumbers.forEach(chartNumbersJson::add);
        JsonArrayBuilder intChartNumbersJson = Json.createArrayBuilder();
        intChartNumbers.forEach(intChartNumbersJson::add);

        return Json.createObjectBuilder()
                .add("seriesIdentifier", getSeriesIdentifier().toJson())
                .add("generalArea", generalArea)
                .add("locality", locality)
                .add("specificLocations", specificLocationsJson)
                .add("chartNumbers", chartNumbersJson)
                .add("intChartNumbers", intChartNumbersJson)
                .add("issueDate", issueDate.getTime());
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

    public List<String> getSpecificLocations() {
        return specificLocations;
    }
    
    public void setSpecificLocations(List<String> specificLocations) {
        this.specificLocations = specificLocations;
    }
    
    public List<String> getChartNumbers() {
        return chartNumbers;
    }
    
    public void setChartNumbers(List<String> chartNumbers) {
        this.chartNumbers = chartNumbers;
    }
    
    public List<Integer> getIntChartNumbers() {
        return intChartNumbers;
    }
    
    public void setIntChartNumbers(List<Integer> intChartNumbers) {
        this.intChartNumbers = intChartNumbers;
    }
    
    public Date getIssueDate() {
        return issueDate;
    }
    
    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }
    
}
