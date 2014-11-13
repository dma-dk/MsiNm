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
package dk.dma.msinm.common.vo;

import dk.dma.msinm.common.model.BaseEntity;

/**
 * Base class for value objects.
 * It essentially defines two ways of using the VO:
 * <ul>
 *     <li>A constructor that takes a BaseEntity is used for converting an entity into a VO.</li>
 *     <li>A {@code toEntity()} method is used for converting the VO back into an entity.</li>
 * </ul>
 */
public abstract class BaseVo<E extends BaseEntity> implements JsonSerializable {

    /**
     * Constructor
     */
    public BaseVo() {
    }

    /**
     * Constructor
     * Use this when the VO is constructed from the entity
     * @param entity the entity of the VO
     */
    public BaseVo(E entity) {
    }

    /**
     * Converts the VO to an entity
     * @return the entity
     */
    public abstract E toEntity();

}
