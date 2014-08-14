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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Used to define what part of a class that should be copied.<br/>
 * Example usage:
 * <ul>
 *     <li>When converting an entity class to a value object (VO).</li>
 *     <li>When pre-loading entity classes before detaching them.</li>
 * </ul>
 * <p></p>
 *
 * The data filter contains two types of information:
 * <ul>
 *     <li>A language: This is assumed to be the same for an entire operation and
 *         specifies which localized description records to include.</li>
 *     <li>A list of fields. The fields can be specific fields, such as "parentId" or more 
 *         abstract specifications such as "details" or "all".<br/>
 *         Also, the fields may have a component prefix, such as "Area.parent" or "*.*" </li>
 * </ul>
 */
public class DataFilter {

    // Standard values for what to include
    public static final String ALL          = "*";
    public static final String PARENT       = "parent";
    public static final String PARENT_ID    = "parentId";
    public static final String CHILDREN     = "children";
    public static final String LOCATIONS    = "locations";

    Set<String> fields = new HashSet<>();
    String lang;

    /**
     * Copy constructor
     * @param other the data filter to copy
     */
    public DataFilter(DataFilter other) {
        this.lang = other.lang;
        this.fields.addAll(other.fields);
    }

    /**
     * Constructor
     * @param fields enlisting of what should be copied
     */
    public DataFilter(String... fields) {
        this.fields.addAll(Arrays.asList(fields));
    }

    /**
     * Factory method that returns a new DataFilter instance based on the given fields
     * @param fields enlisting of what should be copied
     * @return a new DataFilter instance based on the given fields
     */
    public static DataFilter get(String... fields) {
        return new DataFilter(fields);
    }

    /**
     * Factory method that returns a new DataFilter instance specifying a specific language to include
     * @param lang the language to include
     * @return a new DataFilter instance specifying a specific language to include
     */
    public static DataFilter lang(String lang) {
        return new DataFilter().setLang(lang);
    }

    /**
     * Returns a copy of the data filter with fields for the given component.
     * The component part of any field is removed.
     * @param component the component to return the data filter for
     * @return the data filter
     */
    public DataFilter forComponent(String component) {
        if (component == null) {
            return this;
        }
        DataFilter filter = lang(lang);
        fields.forEach(field -> {
            if (!field.contains(".") || !field.startsWith(component + ".")) {
                filter.fields.add(field);
            } else if (field.startsWith(component + ".")) {
                filter.fields.add(field.substring(component.length() + 1));
            }
        });
        return filter;
    }

    /**
     * Returns a copy of the data filter with fields for the given component.
     * The component part of any field is removed.
     * @param componentClass the component to return the data filter for
     * @return the data filter
     */
    public DataFilter forComponent(Class<?> componentClass) {
        return forComponent(componentClass.getSimpleName());
    }

    /**
     * Sets the language to include
     * @param lang the language to include
     * @return this
     */
    public DataFilter setLang(String lang) {
        this.lang = lang;
        return this;
    }

    /**
     * Returns if the given language should be included
     * @param desc the localizable description entity to check
     * @return if the given language should be include
     */
    public boolean includeLang(ILocalizedDesc desc) {
        return lang == null || desc.getLang().equals(lang);
    }

    /**
     * Returns whether to include the given field
     * @param field the field to check
     * @return whether to include the given field
     */
    public boolean include(String field) {
        return fields.contains(ALL) || fields.contains(field);
    }

    /**
     * Returns whether the field is the only one defined for the filter
     * @param field the field
     * @return whether the field is the only one defined for the filter
     */
    public boolean restrictTo(String field) {
        return fields.size() == 1 && fields.contains(field);
    }

    /**
     * Returns whether to include any of the given fields
     * @param fields the field to check
     * @return whether to include any the given fields
     */
    public boolean includeAnyOf(String... fields) {
        if (this.fields.contains(ALL)) {
            return true;
        }
        for (String field : fields) {
            if (this.fields.contains(field)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether to include all of the given fields
     * @param fields the field to check
     * @return whether to include all the given fields
     */
    public boolean includeAllOf(String... fields) {
        if (this.fields.contains(ALL)) {
            return true;
        }
        for (String field : fields) {
            if (!this.fields.contains(field)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Shortcut method that returns whether to include the parent field
     * @return whether to include the parent field
     */
    public boolean includeParent() {
        return include(PARENT);
    }

    /**
     * Shortcut method that returns whether to include the parent id field
     * @return whether to include the parent id field
     */
    public boolean includeParentId() {
        return include(PARENT_ID);
    }

    /**
     * Shortcut method that returns whether to include the children field
     * @return whether to include the children field
     */
    public boolean includeChildren() {
        return include(CHILDREN);
    }


    /**
     * Shortcut method that returns whether to include the locations field
     * @return whether to include the locations field
     */
    public boolean includeLocations() {
        return include(LOCATIONS);
    }
}
