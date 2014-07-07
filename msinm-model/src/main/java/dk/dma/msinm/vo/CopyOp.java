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
package dk.dma.msinm.vo;

import dk.dma.msinm.common.model.ILocalizedDesc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Used to define what part of the entity should be copied to the value object
 */
public class CopyOp {

    // Standard values for what to copy
    public static final String ALL          = "all";
    public static final String PARENT       = "parent";
    public static final String PARENT_ID    = "parentId";
    public static final String CHILDREN     = "children";

    Set<String> types = new HashSet<>();
    String lang;

    /**
     * Constructor
     * @param copyType enlisting of what should be copied
     */
    public CopyOp(String... copyType) {
        types.addAll(Arrays.asList(copyType));
    }

    /**
     * Returns a new CopyOp instance based on the given copyTypes
     * @param copyType enlisting of what should be copied
     * @return a new CopyOp instance based on the given copyTypes
     */
    public static CopyOp get(String... copyType) {
        return new CopyOp(copyType);
    }

    /**
     * Returns a new CopyOp instance specifying a specific language to copy
     * @param lang the language to copy
     * @return a new CopyOp instance specifying a specific language to copy
     */
    public static CopyOp lang(String lang) {
        return new CopyOp().setLang(lang);
    }

    /**
     * Returns whether to copy the given type
     * @param type the type to copy
     * @return whether to copy the given type
     */
    public boolean copy(String type) {
        return ALL.equals(type) || types.contains(type);
    }

    /**
     * Sets the language to copy
     * @param lang the language to copy
     * @return this
     */
    public CopyOp setLang(String lang) {
        this.lang = lang;
        return this;
    }

    /**
     * Returns if the given language should be copied
     * @param desc the localizable description entity to test
     * @return if the given language should be copied
     */
    public boolean copyLang(ILocalizedDesc desc) {
        return lang == null || desc.getLang().equals(lang);
    }
}
