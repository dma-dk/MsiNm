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
 * Entities implementing this interface should thus ensure that all realted
 * entities get preloaded when the {@code preload()} method gets called.
 */
public interface IPreloadable {

    /**
     * Pre-load all related entities
     */
    default void preload() {
        preload(new DataFilter(DataFilter.ALL));
    }

    /**
     * Pre-load all related entities
     * @param dataFilter what type of data to preload
     */
    void preload(DataFilter dataFilter);
}
