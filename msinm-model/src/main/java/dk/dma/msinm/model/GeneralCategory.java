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

/**
 * The General category used for the {@code MessageCategory}.
 * <p>
 * The values are largely based on the standards for MSI and NM:
 * <ul>
 *   <li>IHO-IMO-WMO S-53 standard on MSI (section 7)</li>
 *   <li>IHO S-4 standard (B-620.3)</li>
 * </ul>
 */
public enum GeneralCategory {
    AIDS_TO_NAVIGATION, 
    DANGEROUS_WRECKS,
    UNWIELDY_TOW,
    DRIFTING_HAZARDS,
    SAR_AND_ANTI_POLLUTION_OPERATIONS,
    ISOLATED_DANGERS,
    ROUTE_ALTERATIONS_OR_SUSPENSIONS,
    UNDERWATER_OPERATIONS,
    PIPE_OR_CABLE_LAYING_OPERATIONS,
    RESEARCH_OR_SCIENTIFIC_INSTRUMENTS,
    OFFSHORE_STRUCTURES,
    RADIO_NAVIGATION_SERVICES,
    
    RESTRICTED_OR_REGULATED_AREAS,
    WORK_IN_PROGRESS
    // TODO...
}
