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

import dk.dma.msinm.model.Location;
import dk.dma.msinm.model.SeriesIdType;
import dk.dma.msinm.model.Status;
import dk.dma.msinm.model.Type;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.*;

/**
 * Defines the search parameters
 */
public class MessageSearchParams implements Serializable {

    public enum SortBy { DATE, ID, AREA }
    public enum SortOrder { ASC, DESC }

    String language;
    String query;
    Date from;
    Date to;
    List<Location> locations = new ArrayList<>();
    Status status;
    Set<Type> types = new HashSet<>();
    Set<SeriesIdType> mainTypes = new HashSet<>();
    Set<Integer> areaIds = new HashSet<>();
    Set<Integer> categoryIds = new HashSet<>();
    Set<Integer> chartIds = new HashSet<>();
    boolean bookmarks;

    int maxHits = 100;
    int startIndex = 0;
    SortBy sortBy = SortBy.ID;
    SortOrder sortOrder = SortOrder.DESC;
    boolean mapMode;

    public MessageSearchParams() {
    }

    public MessageSearchParams(String query, Location location) {
        this.query = query;
        if (location != null) {
            this.locations.add(location);
        }
    }

    /**
     * Returns whether or not the search requires a Lucene search
     * @return whether or not the search requires a Lucene search
     */
    public boolean requiresLuceneSearch() {
        return StringUtils.isNotBlank(query) || locations.size() > 0;
    }

    @Override
    public String toString() {
        return "MessageSearchParams{" +
                "language='" + language + '\'' +
                ", query='" + query + '\'' +
                ", from=" + from +
                ", to=" + to +
                ", locations=" + locations +
                ", areaIds=" + areaIds +
                ", categoryIds=" + categoryIds +
                ", chartIds=" + chartIds +
                ", status=" + status +
                ", bookmarks=" + bookmarks +
                ", maxHits=" + maxHits +
                ", startIndex=" + startIndex +
                '}';
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
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

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    public Set<Integer> getAreaIds() {
        return areaIds;
    }

    public void setAreaIds(Set<Integer> areaIds) {
        this.areaIds = areaIds;
    }

    public Set<Integer> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(Set<Integer> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public Set<Integer> getChartIds() {
        return chartIds;
    }

    public void setChartIds(Set<Integer> chartIds) {
        this.chartIds = chartIds;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(boolean bookmarks) {
        this.bookmarks = bookmarks;
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

    public Set<Type> getTypes() {
        return types;
    }

    public void setTypes(Set<Type> types) {
        this.types = types;
    }

    public Set<SeriesIdType> getMainTypes() {
        return mainTypes;
    }

    public void setMainTypes(Set<SeriesIdType> mainTypes) {
        this.mainTypes = mainTypes;
    }

    public SortBy getSortBy() {
        return sortBy;
    }

    public void setSortBy(SortBy sortBy) {
        this.sortBy = sortBy;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isMapMode() {
        return mapMode;
    }

    public void setMapMode(boolean mapMode) {
        this.mapMode = mapMode;
    }
}
