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

import dk.dma.msinm.model.MessageLocation;
import dk.dma.msinm.model.MessageStatus;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * Defines the search parameters
 */
public class MessageSearchParams implements Serializable {

    String query;
    Date from;
    Date to;
    MessageLocation location;
    MessageStatus status;

    int maxHits = 100; // For now...
    int startIndex = 0;

    public MessageSearchParams() {
    }

    public MessageSearchParams(String query, Date from, Date to, MessageLocation location, MessageStatus status) {
        this.query = query;
        this.from = from;
        this.to = to;
        this.location = location;
        this.status = status;
    }

    public MessageSearchParams(String query, MessageLocation location) {
        this.query = query;
        this.location = location;
    }

    /**
     * Returns whether or not the search requires a Lucene search
     * @return whether or not the search requires a Lucene search
     */
    public boolean requiresLuceneSearch() {
        return StringUtils.isNotBlank(query) || location != null;
    }

    @Override
    public String toString() {
        return "MessageSearchParams{" +
                "query='" + query + '\'' +
                ", from=" + from +
                ", to=" + to +
                ", location=" + location +
                ", status=" + status +
                ", maxHits=" + maxHits +
                ", startIndex=" + startIndex +
                '}';
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public MessageLocation getLocation() {
        return location;
    }

    public void setLocation(MessageLocation location) {
        this.location = location;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public int getMaxHits() {
        return maxHits;
    }

    public void setMaxHits(int maxHits) {
        this.maxHits = maxHits;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }
}
