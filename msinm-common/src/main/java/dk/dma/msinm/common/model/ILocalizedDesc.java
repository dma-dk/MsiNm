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

/**
 * Interface to be implemented by the descriptive entities of localizable entities
 */
public interface ILocalizedDesc<E extends ILocalizable> {

    String getLang();

    void setLang(String lang);

    /**
     * Copies the description values from the desc entity to this entity
     * @param desc the description entity to copy from
     */
    void copyDesc(ILocalizedDesc desc);

}
