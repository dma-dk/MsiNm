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

import javax.persistence.Embeddable;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * A unique identifier for an MSI or NtM message
 */
@Embeddable
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "mainType", "authority", "number", "year"}))
public class SeriesIdentifier implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    private SeriesIdType mainType;

    @NotNull
    private String authority;
    
    @NotNull
    private Integer number;
    
    @NotNull
    private Integer year;

    @Override
    public String toString() {
        return String.format("[%s %s %d %d]", mainType, authority, number, year);
    }

    /**
     * Returns a short id for the series identifier with the format DK-184-14
     * @return a short id for the series identifier
     */
    @Transient
    public String getShortId() {
        return String.format("%s-%03d-%02d", authority, number, year - 2000);
    }

    /**
     * Returns a full id for the series identifier with the format MSI-DK-184-14
     * @return a full id for the series identifier
     */
    @Transient
    public String getFullId() {
        return String.format("%s-%s-%03d-%02d", mainType, authority, number, year - 2000);
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
