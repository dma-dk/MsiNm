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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dk.dma.msinm.common.audit.Auditor;
import dk.dma.msinm.common.settings.Setting;
import dk.dma.msinm.common.settings.Settings;
import dk.dma.msinm.common.settings.SettingsEntity;
import dk.dma.msinm.common.util.TimeUtils;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.slf4j.Logger;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.*;
import javax.inject.Inject;
import javax.ws.rs.*;
import java.io.Serializable;
import java.util.Date;


/**
 * Provides an interface for configuring import of Danish legacy MSI warnings.
 * <p/>
 * Sets up a timer service which performs the legacy import every minute.
 */
@Singleton
@Startup
@Path("/import/legacy-msi")
@SecurityDomain("msinm-policy")
@PermitAll
public class LegacyMsiImportRestService {

    @Inject
    private Logger log;

    @Inject
    Auditor auditor;

    @Inject
    LegacyMsiImportService legacyMsiImportService;

    @Inject
    Settings settings;

    /**
     * Returns the legacy import status
     */
    @GET
    @Path("/import-status")
    @Produces("application/json")
    public LegacyMsiImportVo getImportStatus() {
        LegacyMsiImportVo status = new LegacyMsiImportVo();
        status.setActive(settings.getBoolean(LegacyMsiImportService.LEGACY_MSI_ACTIVE));
        status.setStartDate(settings.getDate(LegacyMsiImportService.LEGACY_MSI_START_DATE));
        status.setLastUpdate(settings.getDate(LegacyMsiImportService.LEGACY_MSI_LAST_UPDATE));
        return status;
    }

    /**
     * Updates the legacy import status
     * @param status the legacy import status
     * @return the updated import status
     */
    @PUT
    @Path("/import-status")
    @Consumes("application/json")
    @Produces("application/json")
    @RolesAllowed({ "sysadmin" })
    public LegacyMsiImportVo setImportStatus(LegacyMsiImportVo status) {
        log.info("Setting legacy MSI import type " + status);

        // Update the active setting
        updateSetting(LegacyMsiImportService.LEGACY_MSI_ACTIVE, String.valueOf(status.isActive()));

        // Update the active setting
        Date prevStartDate = settings.getDate(LegacyMsiImportService.LEGACY_MSI_START_DATE);
        Date lastUpdate = settings.getDate(LegacyMsiImportService.LEGACY_MSI_LAST_UPDATE);
        Date startDate = TimeUtils.resetTime(status.getStartDate());

        if (startDate != null && !startDate.equals(prevStartDate)) {
            updateSetting(LegacyMsiImportService.LEGACY_MSI_START_DATE, String.valueOf(startDate.getTime()));

            // If the new start date is before the previous start date, reset the last-update date.
            // Also, if the current last-update date is before the new start date, reset it.
            if (startDate.before(prevStartDate) || startDate.after(lastUpdate)) {
                log.info("Start date moved back in time. Reset last-update time");
                updateSetting(LegacyMsiImportService.LEGACY_MSI_LAST_UPDATE, String.valueOf(startDate.getTime()));
            }
        }

        return getImportStatus();
    }

    /**
     * Updates the settings database
     * @param settingDef the setting to update
     * @param value the new value
     */
    private void updateSetting(Setting settingDef, String value) {
        SettingsEntity setting = new SettingsEntity();
        setting.setKey(settingDef.getSettingName());
        setting.setValue(value);
        settings.updateSetting(setting);
    }

    /**
     * Imports the next batch of legacy MSI messages.
     *
     * @return the number of new or updated warnings
     */
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("/import")
    @Lock(LockType.READ)
    public int importLegacyMsi() {
        return legacyMsiImportService.importAllMsiWarnings().size();
    }

    /**
     * Called every minute to import the legacy MSI warnings
     * @return the number of new or updated warnings
     */
    @Schedule(persistent = false, second = "13", minute = "*/1", hour = "*", dayOfWeek = "*", year = "*")
    public int periodicLegacyMsiImport() {
        return importLegacyMsi();
    }


    /**
     * Helper class that contains information about the state of the legacy MSI integration
     */
    @JsonIgnoreProperties(ignoreUnknown=true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class LegacyMsiImportVo implements Serializable {
        boolean active;
        Date startDate;
        Date lastUpdate;

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public Date getStartDate() {
            return startDate;
        }

        public void setStartDate(Date startDate) {
            this.startDate = startDate;
        }

        public Date getLastUpdate() {
            return lastUpdate;
        }

        public void setLastUpdate(Date lastUpdate) {
            this.lastUpdate = lastUpdate;
        }

        @Override
        public String toString() {
            return "LegacyMsiImportVo{" +
                    "active=" + active +
                    ", startDate=" + startDate +
                    ", lastUpdate=" + lastUpdate +
                    '}';
        }
    }
}
