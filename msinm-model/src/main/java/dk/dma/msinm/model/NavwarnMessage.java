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

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An MSI specialization of the {@code Message} class.
 */
@Entity
public class NavwarnMessage extends Message {

    private static final long serialVersionUID = 1L;

    private Date cancellationDate;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<MessageSeriesIdentifier> cancellations = new HashSet<>();

    @NotNull
    @OneToMany(cascade = CascadeType.ALL)
    private List<MessageItem> messageItems = new ArrayList<>();

    @NotNull
    private Priority priority = Priority.NONE;

    public NavwarnMessage() {
    }

    /**
     * Creates a Json representation of this entity
     *
     * @return the Json representation
     */
    public JsonObjectBuilder toJson() {
        JsonArrayBuilder messageItemsJson = Json.createArrayBuilder();
        messageItems.forEach(item -> messageItemsJson.add(item.toJson()));
        JsonArrayBuilder cancellationsJson = Json.createArrayBuilder();
        cancellations.forEach(c -> cancellationsJson.add(c.toJson()));

        JsonObjectBuilder json = super.toJson();
        if (cancellationDate != null) {
            json.add("cancellationDate", cancellationDate.getTime());
        }
        json.add("cancellations", cancellationsJson)
                .add("priority", priority.toString())
                .add("messageItems", messageItemsJson);

        return json;
    }

    /**
     * ***** Getters and setters ********
     */


    public Date getCancellationDate() {
        return cancellationDate;
    }

    public void setCancellationDate(Date cancellationDate) {
        this.cancellationDate = cancellationDate;
    }

    public Set<MessageSeriesIdentifier> getCancellations() {
        return cancellations;
    }

    public void setCancellations(Set<MessageSeriesIdentifier> cancellations) {
        this.cancellations = cancellations;
    }

    public List<MessageItem> getMessageItems() {
        return messageItems;
    }

    public void setMessageItems(List<MessageItem> messageItems) {
        this.messageItems = messageItems;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }
}
