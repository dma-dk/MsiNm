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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * A unique identifier for an MSI or NtM message
 */
@Embeddable
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class SeriesIdentifier implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Enumerated(EnumType.STRING)
    private SeriesIdType mainType;

    @NotNull
    private String authority;
    
    private Integer number;
    
    @NotNull
    private Integer year;

    @Override
    public String toString() {
        return String.format("[%s]", getFullId());
    }

    /**
     * Returns a short id for the series identifier with the format DK-184-14
     * @return a short id for the series identifier
     */
    @Transient
    public String getShortId() {
        return (number != null)
                ? String.format("%s-%03d-%02d", authority, number, year - 2000)
                : String.format("%s-?-%02d", authority, year - 2000);
    }

    /**
     * Returns a full id for the series identifier with the format MSI-DK-184-14
     * @return a full id for the series identifier
     */
    @Transient
    public String getFullId() {
        return String.format("%s-%s", mainType, getShortId());
    }

    // *** Getters and setters

    public SeriesIdType getMainType() {
        return mainType;
    }

    public void setMainType(SeriesIdType mainType) {
        this.mainType = mainType;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

}
