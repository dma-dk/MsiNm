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
package dk.dma.msinm.legacy.service;

import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import java.util.ArrayList;
import java.util.List;

import dk.frv.enav.msi.ws.warning.MsiService;
import dk.frv.enav.msi.ws.warning.WarningService;
import dk.frv.msiedit.core.webservice.message.MsiDto;


/**
 * Provides an interface for fetching active MSI warnings from the legacy MSI system.
 * <p/>
 * Sets up a timer service which performs the legacy import every 5 minutes.
 */
@Singleton
@Startup
public class LegacyMsiImportService {

    private static final String MSI_WSDL = "/wsdl/warning.wsdl";

    @Inject
    private Logger log;

    @EJB
    LegacyMessageService legacyMessageService;

    //String endpoint = "http://msi-beta.e-navigation.net/msi/ws/warning";
    String endpoint = "http://msi.dma.dk/msi/ws/warning";
    String countries = "DK";

    private MsiService msiService;

    /**
     * Called when the bean is initialized
     */
    @PostConstruct
    public void init() {
        msiService = new WarningService(
                getClass().getResource(MSI_WSDL),
                new QName("http://enav.frv.dk/msi/ws/warning", "WarningService"))
                .getMsiServiceBeanPort();
        log.info("Initialized MSI webservice");
    }

    /**
     * Returns the current list of active MSI warnings
     *
     * @return the current list of active MSI warnings
     */
    public List<MsiDto> getActiveWarnings() {

        // Update the WS endpoint
        ((BindingProvider) msiService).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);

        List<MsiDto> result = new ArrayList<>();

        boolean error = false;
        for (String country : countries.split(",")) {
            int count = 0;
            try {
                for (MsiDto md : msiService.getActiveWarningCountry(country).getItem()) {
                    result.add(md);
                    count++;
                }
            } catch (Throwable t) {
                log.error("Error reading warnings from MSI service: " + endpoint + " for country: " + country, t);
                error = true;
            }
            log.info("Read " + count + " warnings from MSI provider: " + country);
        }
        log.info("Read " + result.size() + " warnings from MSI service: " + endpoint);
        if (error) {
            log.error("There was a problem reading MSI warnings from endpoint " + endpoint);
            throw new RuntimeException();
        }
        return result;

    }

    /**
     * Called every 5 minutes to import the legacy MSI warnings
     * @return the number of new or updated warnings
     */
    @Schedule(persistent = false, second = "13", minute = "*/5", hour = "*", dayOfWeek = "*", year = "*")
    public int importWarnings() {

        int newOrUpdatedWarnings = 0;

        List<MsiDto> warnings = getActiveWarnings();
        log.info("Fetched " + warnings.size() + " legacy MSI warnings");

        for (MsiDto msi : warnings) {
            boolean update = legacyMessageService.createOrUpdateNavwarnMessage(msi);
            if (update) {
                newOrUpdatedWarnings++;
            }
        }
        return newOrUpdatedWarnings;
    }

}

