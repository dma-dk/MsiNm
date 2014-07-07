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

import dk.dma.msinm.common.vo.BaseVo;
import dk.dma.msinm.model.Reference;
import dk.dma.msinm.model.ReferenceType;
import dk.dma.msinm.model.SeriesIdentifier;

/**
 * Value object for the {@code Reference} model entity
 */
public class ReferenceVo extends BaseVo<Reference> {

    Integer id;
    SeriesIdentifier seriesIdentifier;
    ReferenceType type;

    /**
     * Constructor
     */
    public ReferenceVo() {
    }

    /**
     * Constructor
     * @param reference the reference
     */
    public ReferenceVo(Reference reference) {
        super(reference);

        id = reference.getId();

        seriesIdentifier = reference.getSeriesIdentifier();
        type = reference.getType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reference toEntity() {
        Reference reference = new Reference();
        reference.setId(id);
        reference.setSeriesIdentifier(seriesIdentifier);
        reference.setType(type);
        return reference;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public SeriesIdentifier getSeriesIdentifier() {
        return seriesIdentifier;
    }

    public void setSeriesIdentifier(SeriesIdentifier seriesIdentifier) {
        this.seriesIdentifier = seriesIdentifier;
    }

    public ReferenceType getType() {
        return type;
    }

    public void setType(ReferenceType type) {
        this.type = type;
    }
}
