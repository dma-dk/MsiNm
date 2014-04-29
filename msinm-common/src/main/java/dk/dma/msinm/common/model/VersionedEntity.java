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
package dk.dma.msinm.common.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Base class for versioned entity beans.
 *
 * The created and updated fields will automatically be updated upon persisting the entity.
 */
@MappedSuperclass
public abstract class VersionedEntity<K extends Serializable> extends BaseEntity<K> {

    @Version
    @Column(name="version")
    int version;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="created")
    Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="updated")
    Date updated;

    @PrePersist
    protected void onCreate() {
        updated = new Date();
        if (created == null) {
            created = updated;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updated = new Date();
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }
}
