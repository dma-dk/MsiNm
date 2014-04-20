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
package dk.dma.msinm.model;

import dk.dma.msinm.common.model.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

/**
 * The message category
 */
@Entity
public class MessageCategory extends BaseEntity<Integer> {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Enumerated(EnumType.STRING)
    private GeneralCategory generalCategory;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    private SpecificCategory specificCategory;
    
    @NotNull
    private String otherCategory;

    /**
     * Constructor
     */
    public MessageCategory() {
    }

    /******** Getters and setters *********/

    public GeneralCategory getGeneralCategory() {
        return generalCategory;
    }

    public void setGeneralCategory(GeneralCategory generalCategory) {
        this.generalCategory = generalCategory;
    }

    public SpecificCategory getSpecificCategory() {
        return specificCategory;
    }

    public void setSpecificCategory(SpecificCategory specificCategory) {
        this.specificCategory = specificCategory;
    }

    public String getOtherCategory() {
        return otherCategory;
    }

    public void setOtherCategory(String otherCategory) {
        this.otherCategory = otherCategory;
    }

}
