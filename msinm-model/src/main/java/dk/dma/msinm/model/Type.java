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

/**
 * Message type for message series identifier
 */
public enum Type {
    // NtM types
    PERMANENT_NOTICE(false),
    TEMPORARY_NOTICE(false),
    PRELIMINARY_NOTICE(false),
    MISCELLANEOUS_NOTICE(false),
    
    // MSI types
    COSTAL_WARNING(true),
    SUBAREA_WARNING(true),
    NAVAREA_WARNING(true);

    boolean msi;

    private Type(boolean msi) {
        this.msi = msi;
    }

    public boolean isMsi() {
        return msi;
    }

    public boolean isNm() {
        return !msi;
    }

    public String getPrefix() {
        return isMsi() ? "MSI" : "NM";
    }

    public SeriesIdType getSeriesIdType() {
        return isMsi() ? SeriesIdType.MSI : SeriesIdType.NM;
    }
}
