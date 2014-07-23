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
package dk.dma.msinm.legacy.msi.service;

import dk.dma.msinm.common.audit.Auditor;
import dk.dma.msinm.common.settings.DefaultSetting;
import dk.dma.msinm.common.settings.Setting;
import dk.dma.msinm.common.settings.Settings;
import dk.dma.msinm.common.settings.SettingsEntity;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.slf4j.Logger;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.*;
import javax.inject.Inject;
import javax.ws.rs.*;


/**
 * Provides an interface for configuring import of Danish legacy MSI warnings.
 * <p/>
 * Sets up a timer service which performs the legacy import every 5 minutes.
 * <p></p>
 * The imported legacy MSI warnings depends on the current MSI import type:
 * <ul>
 *     <li>If the type is "none", nothing happens.</li>
 *     <li>If the type is "active" only active legacy MSI are imported.</li>
 *     <li>If the type is "all", both active and inactive legacy MSI are imported.</li>
 * </ul>
 */
@Singleton
@Startup
@Path("/import/legacy-msi")
@SecurityDomain("msinm-policy")
@PermitAll
public class LegacyMsiImportRestService {

    static final Setting LEGACY_MSI_IMPORT_TYPE   = new DefaultSetting("legacyMsiImportType", "none");

    @Inject
    private Logger log;

    @Inject
    Auditor auditor;

    @Inject
    LegacyMsiImportService legacyMsiImportService;

    @Inject
    Settings settings;

    /**
     * Returns the current legacy MSI import type
     * @return the current legacy MSI import type
     */
    public LegacyMsiImportType getLegacyMsiImportType() {
        String type = settings.get(LEGACY_MSI_IMPORT_TYPE);
        return LegacyMsiImportType.get(type);
    }

    /**
     * Returns the legacy import type
     */
    @GET
    @Path("/import-type")
    @Produces("application/json")
    public String getImportType() {
        return getLegacyMsiImportType().name();
    }

    /**
     * Updates the legacy import type
     * @param type the legacy import type
     */
    @PUT
    @Path("/import-type")
    @RolesAllowed({ "admin" })
    public String setImportType(@FormParam("type") String type) {
        log.info("Setting legacy MSI import type " + type);

        // Make sure the value is valid
        type = LegacyMsiImportType.get(type).name();

        // Update the setting
        SettingsEntity setting = new SettingsEntity();
        setting.setKey(LEGACY_MSI_IMPORT_TYPE.getSettingName());
        setting.setValue(type);
        settings.updateSetting(setting);
        return "OK";
    }


    /**
     * Imports the legacy MSI warnings depending on the current MSI import type:
     * <ul>
     *     <li>If the type is "none", nothing happens.</li>
     *     <li>If the type is "active" only active legacy MSI are imported.</li>
     *     <li>If the type is "all", both active and inactive legacy MSI are imported.</li>
     * </ul>
     * @return the number of new or updated warnings
     */
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("/import")
    @Lock(LockType.READ)
    public int importLegacyMsi() {
        LegacyMsiImportType type = getLegacyMsiImportType();
        int count = 0;

        // Check if we need to import active legacy MSI
        if (type == LegacyMsiImportType.ACTIVE || type == LegacyMsiImportType.ALL) {
            count += legacyMsiImportService.importActiveMsiWarnings().size();
        }

        // Check if we need to import old inactive legacy MSI
        if (type == LegacyMsiImportType.ALL) {
            // TODO
        }

        return count;
    }

    /**
     * Called every 5 minutes to import the legacy MSI warnings
     * @return the number of new or updated warnings
     */
    @Schedule(persistent = false, second = "13", minute = "*/5", hour = "*", dayOfWeek = "*", year = "*")
    public int periodicLegacyMsiImport() {
        return importLegacyMsi();
    }

}
