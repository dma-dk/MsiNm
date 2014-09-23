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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dk.dma.msinm.model.Location;
import dk.dma.msinm.model.SeriesIdType;
import dk.dma.msinm.model.Status;
import dk.dma.msinm.model.Type;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Defines the search parameters
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class MessageSearchParams implements Serializable {

    public static final String DATE_FORMAT = "dd-MM-yyyy";

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

    Date updatedFrom;
    Date updatedTo;

    /**
     * Constructor
     */
    public MessageSearchParams() {
    }

    /**
     * Constructor
     * @param query the query
     * @param location the location
     */
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
        return isNotBlank(query) || locations.size() > 0;
    }

    /**
     * Returns a string representation of the filtering criteria (not sorting or paging)
     * @return a string representation of the filtering criteria
     */
    @Override
    public String toString() {
        List<String> desc = new ArrayList<>();
        if (isNotBlank(language)) { desc.add(String.format("Language: %s", language)); }
        if (isNotBlank(query)) { desc.add(String.format("Query: '%s'", query)); }
        if (from != null) { desc.add(String.format("From: %s", new SimpleDateFormat(DATE_FORMAT).format(from))); }
        if (to != null) { desc.add(String.format("To: %s", new SimpleDateFormat(DATE_FORMAT).format(to))); }
        if (locations.size() > 0) { desc.add(String.format("%d locations", locations.size())); }
        if (status != null) { desc.add(String.format("Status: %s", status)); }
        if (types.size() > 0) { desc.add(String.format("Types: %s", types)); }
        if (mainTypes.size() > 0) { desc.add(String.format("Main types: %s", mainTypes)); }
        if (areaIds.size() > 0) { desc.add(String.format("Area ID's: %s", areaIds)); }
        if (categoryIds.size() > 0) { desc.add(String.format("Category ID's: %s", categoryIds)); }
        if (chartIds.size() > 0) { desc.add(String.format("Chart ID's: %s", chartIds)); }
        if (bookmarks) { desc.add("Bookmarks: true"); }
        if (updatedFrom != null) { desc.add(String.format("Updated from: %s", updatedFrom)); }
        if (updatedTo != null) { desc.add(String.format("Updated to: %s", updatedTo)); }

        return desc.stream().collect(Collectors.joining(", "));
    }

    /**
     * Parses the request parameters and collects them in a MessageSearchParams entity
     */
    public static MessageSearchParams readParams(
            String language,
            String query,
            String status,
            String type,
            String loc,
            String areas,
            String categories,
            String charts,
            String fromDate,
            String toDate,
            int maxHits,
            int startIndex,
            String sortBy,
            String sortOrder,
            boolean mapMode
    ) throws ParseException {

        MessageSearchParams params = new MessageSearchParams();
        params.setLanguage(language);
        params.setStartIndex(startIndex);
        params.setMaxHits(maxHits);
        params.setMapMode(mapMode);
        params.setSortBy(MessageSearchParams.SortBy.valueOf(sortBy));
        params.setSortOrder(MessageSearchParams.SortOrder.valueOf(sortOrder));
        params.setQuery(query);

        if (isNotBlank(status)) {
            // Special case for "BOOKMARKED" status
            if ("BOOKMARKED".equalsIgnoreCase(status)) {
                params.setBookmarks(true);
            } else {
                params.setStatus(Status.valueOf(status));
            }
        }

        if (isNotBlank(type)) {
            for (String msgType : type.split(",")) {
                if (msgType.equals("MSI") || msgType.equals("NM")) {
                    params.getMainTypes().add(SeriesIdType.valueOf(msgType));
                } else {
                    params.getTypes().add(Type.valueOf(msgType));
                }
            }
        }

        if (isNotBlank(loc)) {
            params.setLocations(Location.fromJson(loc));
        }

        if (isNotBlank(areas)) {
            for (String areaId : areas.split(",")) {
                params.getAreaIds().add(Integer.valueOf(areaId));
            }
        }

        if (isNotBlank(categories)) {
            for (String categoryId : categories.split(",")) {
                params.getCategoryIds().add(Integer.valueOf(categoryId));
            }
        }

        if (isNotBlank(charts)) {
            for (String chartId : charts.split(",")) {
                params.getChartIds().add(Integer.valueOf(chartId));
            }
        }

        if (isNotBlank(fromDate)) {
            params.setFrom(new SimpleDateFormat(DATE_FORMAT).parse(fromDate));
        }

        if (isNotBlank(toDate)) {
            params.setTo(new SimpleDateFormat(DATE_FORMAT).parse(toDate));
        }

        return params;
    }

    /**
     * Parses the request parameters and collects them in a MessageSearchParams entity
     */
    public static MessageSearchParams readParams(
            String query,
            String status,
            String type,
            String loc,
            String areas,
            String categories,
            String charts,
            String sortBy,
            String sortOrder
    ) throws ParseException {
        return readParams(null, query, status, type, loc, areas, categories, charts, null, null, 999999, 0, sortBy, sortOrder, false);
    }

    // *****************************
    // *** Getters and setters
    // *****************************

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

    public Date getUpdatedFrom() {
        return updatedFrom;
    }

    public void setUpdatedFrom(Date updatedFrom) {
        this.updatedFrom = updatedFrom;
    }

    public Date getUpdatedTo() {
        return updatedTo;
    }

    public void setUpdatedTo(Date updatedTo) {
        this.updatedTo = updatedTo;
    }
}
