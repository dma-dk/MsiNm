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
package dk.dma.msinm.legacy.msi.model;

import dk.dma.msinm.common.model.BaseEntity;
import dk.dma.msinm.model.Message;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Records various information about the legacy MSI messages imported.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "legacyId"}))
@NamedQueries({
        @NamedQuery(name= "LegacyMessage.findByLegacyId",
                query="SELECT msg FROM LegacyMessage msg where msg.legacyId = :legacyId"),
        @NamedQuery(name= "LegacyMessage.findByLegacyMessageId",
                query="SELECT msg FROM LegacyMessage msg where msg.legacyMessageId = :legacyMessageId ORDER BY msg.version DESC")
})
public class LegacyMessage extends BaseEntity<Integer> {

    private static final long serialVersionUID = 1L;

    @NotNull
    private Integer legacyId;

    private Integer legacyMessageId;

    private String navtexNo;

    @NotNull
    private Integer version;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    private Message message;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    Date updated;

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

    public Integer getLegacyMessageId() {
        return legacyMessageId;
    }

    public void setLegacyMessageId(Integer legacyMessageId) {
        this.legacyMessageId = legacyMessageId;
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

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }
}
