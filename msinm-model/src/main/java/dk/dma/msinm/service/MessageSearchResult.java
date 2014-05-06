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
package dk.dma.msinm.service;

import dk.dma.msinm.model.Message;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the message search result.
 * <p>
 * The search result is paged, as per the {@code MessageSearchParams},
 * so, the messages returned constitutes a subset of the {@code total}
 * number of messages starting af the {@code startIndex}.
 */
public class MessageSearchResult implements Serializable {

    List<Message> messages = new ArrayList<>();
    int startIndex;
    int total;

    /**
     * Creates a Json representation of this entity
     * @return the Json representation
     */
    public JsonObjectBuilder toJson() {
        JsonArrayBuilder messagesJson = Json.createArrayBuilder();
        messages.forEach(msg -> messagesJson.add(msg.toJson()));

        return Json.createObjectBuilder()
                .add("messages", messagesJson)
                .add("startIndex", startIndex)
                .add("total", total);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "MessageSearchResult{" +
                "messages no=" + messages.size() +
                ", startIndex=" + startIndex +
                ", total=" + total +
                '}';
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
