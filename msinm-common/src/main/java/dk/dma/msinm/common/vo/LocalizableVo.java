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
import dk.dma.msinm.common.model.ILocalizable;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for localizable VO's
 */
public abstract class LocalizableVo<E extends BaseEntity & ILocalizable, D extends LocalizedDescVo> extends BaseVo<E> implements ILocalizable<D> {

    List<D> descs;

    /**
     * Constructor
     */
    public LocalizableVo() {
        super();
    }

    /**
     * Constructor
     * @param entity the entity of the VO
     */
    public LocalizableVo(E entity) {
        super(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<D> getDescs() {
        return descs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDescs(List<D> descs) {
        this.descs = descs;
    }

}
