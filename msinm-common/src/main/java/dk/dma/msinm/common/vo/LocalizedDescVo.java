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
import dk.dma.msinm.common.model.ILocalizedDesc;

/**
 * Base class for VO's of localizable entities
 */
public abstract class LocalizedDescVo <E extends BaseEntity & ILocalizedDesc, L extends LocalizableVo> extends BaseVo<E> implements ILocalizedDesc<L> {

    String lang;

    /**
     * Constructor
     */
    public LocalizedDescVo() {
        super();
    }

    /**
     * Constructor
     * @param entity the entity of the VO
     */
    public LocalizedDescVo(E entity) {
        super(entity);
        this.lang = entity.getLang();
    }

    @Override
    public String getLang() {
        return lang;
    }

    @Override
    public void setLang(String lang) {
        this.lang = lang;
    }

    @Override
    public void copyDesc(ILocalizedDesc desc) {
        // Not used for VO's
    }
}
