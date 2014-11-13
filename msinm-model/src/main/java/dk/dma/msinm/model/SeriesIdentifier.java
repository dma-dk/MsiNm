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

import dk.dma.msinm.common.vo.JsonSerializable;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

/**
 * A unique identifier for an MSI or NtM message
 */
@Embeddable
public class SeriesIdentifier implements JsonSerializable {

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

    /**
     * Returns a copy of this SeriesIdentifier
     * @return a copy of this SeriesIdentifier
     */
    public SeriesIdentifier copy() {
        SeriesIdentifier id = new SeriesIdentifier();
        id.mainType = mainType;
        id.authority = authority;
        id.number = number;
        id.year = year;
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SeriesIdentifier)) return false;

        SeriesIdentifier that = (SeriesIdentifier) o;

        if (authority != null ? !authority.equals(that.authority) : that.authority != null) return false;
        if (mainType != that.mainType) return false;
        if (number != null ? !number.equals(that.number) : that.number != null) return false;
        if (year != null ? !year.equals(that.year) : that.year != null) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = mainType != null ? mainType.hashCode() : 0;
        result = 31 * result + (authority != null ? authority.hashCode() : 0);
        result = 31 * result + (number != null ? number.hashCode() : 0);
        result = 31 * result + (year != null ? year.hashCode() : 0);
        return result;
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
