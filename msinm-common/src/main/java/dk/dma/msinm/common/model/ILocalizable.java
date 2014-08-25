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
import java.util.ArrayList;
import java.util.List;

/**
 * Interface to be implemented by localized entities
 */
public interface ILocalizable<D extends ILocalizedDesc> {

    /**
     * Returns the list of localized descriptions
     * @return the list of localized descriptions
     */
    public List<D> getDescs();

    /**
     * Sets the list of localized descriptions
     * @param descs the list of localized descriptions
     */
    public void setDescs(List<D> descs);

    /**
     * Returns the list of localized descriptions as specified by the data filter.
     * <p>
     *     If no description matches the filter, the first available description is included.
     * </p>
     *
     * @param dataFilter defines the languages to include from the entity
     * @return the list of localized descriptions as specified by the data filter
     */
    @Transient
    default public List<D> getDescs(DataFilter dataFilter) {
        // Sanity checks
        if (dataFilter == null || getDescs() == null) {
            return getDescs();
        }

        // Collect the matching descriptions
        List<D> result = new ArrayList<>();
        getDescs().stream()
                .filter(dataFilter::includeLang)
                .forEach(result::add);

        // If no match is found, pick the first available
        if (result.isEmpty() && !getDescs().isEmpty()) {
            result.add(getDescs().get(0));
        }
        return result;
    }

    /**
     * Returns the list of localized descriptions and creates the list if necessary
     * @return the list of localized descriptions
     */
    default public List<D> checkCreateDescs() {
        if (getDescs() == null) {
            setDescs(new ArrayList<>());
        }
        return getDescs();
    }

    /**
     * Returns the localized description for the given language.
     * Returns null if the description is not defined.
     *
     * @param lang the language
     * @return the localized description for the given language
     */
    @Transient
    default public D getDesc(String lang) {
        if (getDescs() != null) {
            for (D desc : getDescs()) {
                if (desc.getLang().equalsIgnoreCase(lang)) {
                    return desc;
                }
            }
        }
        return null;
    }

    /**
     * Creates the localized description for the given language
     * and adds it to the list of description entities.
     *
     * @param lang the language
     * @return the created description
     */
    public D createDesc(String lang);


    /**
     * Returns the localized description for the given language.
     * Creates a new description entity if none exists in advance.
     *
     * @param lang the language
     * @return the localized description for the given language
     */
    @Transient
    default public D checkCreateDesc(String lang) {
        D desc = getDesc(lang);
        if (desc == null) {
            desc = createDesc(lang);
        }
        return desc;
    }


    /**
     * Copies the descriptive fields of the list of descriptions
     * @param descs the description entities to copy
     */
    default public void copyDescs(List<D> descs) {
        if (descs != null && descs.size() > 0) {
            descs.forEach(desc -> checkCreateDesc(desc.getLang()).copyDesc(desc));
        }
    }

    /**
     * Copies the descriptive fields of the list of descriptions.
     * Subsequently removes descriptive entities left blank.
     * @param descs the description entities to copy
     */
    default public void copyDescsAndRemoveBlanks(List<D> descs) {
        copyDescs(descs);

        // Remove descriptive entities left blank
        if (getDescs() != null) {
            getDescs().removeIf(desc -> !desc.descDefined());
        }
    }
}
