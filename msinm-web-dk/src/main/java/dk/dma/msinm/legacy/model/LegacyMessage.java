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
package dk.dma.msinm.legacy.model;

import dk.dma.msinm.common.model.BaseEntity;
import dk.dma.msinm.model.NavwarnMessage;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Records various information about the legacy MSI messages imported.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "legacyId"}))
@NamedQueries({
        @NamedQuery(name= "LegacyMessage.findByLegacyId",
                query="SELECT msg FROM LegacyMessage msg where msg.legacyId = :legacyId")
})
public class LegacyMessage extends BaseEntity<Integer> {

    private static final long serialVersionUID = 1L;

    @NotNull
    private Integer legacyId;

    private String navtexNo;

    @NotNull
    private Integer version;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    private NavwarnMessage navwarnMessage;

    /**
     * Constructor
     */
    public LegacyMessage() {
    }

    /************ Getters and setters **************/

    public Integer getLegacyId() {
        return legacyId;
    }

    public void setLegacyId(Integer legacyId) {
        this.legacyId = legacyId;
    }

    public String getNavtexNo() {
        return navtexNo;
    }

    public void setNavtexNo(String navtexNo) {
        this.navtexNo = navtexNo;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public NavwarnMessage getNavwarnMessage() {
        return navwarnMessage;
    }

    public void setNavwarnMessage(NavwarnMessage navwarnMessage) {
        this.navwarnMessage = navwarnMessage;
    }
}
