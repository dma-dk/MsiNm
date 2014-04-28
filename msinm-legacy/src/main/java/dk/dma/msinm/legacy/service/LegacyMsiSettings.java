package dk.dma.msinm.legacy.service;

import dk.dma.msinm.common.settings.BaseSettings;

import javax.inject.Singleton;

/**
 * Defines the settings used by the legacy MSI module
 */
@Singleton
public class LegacyMsiSettings extends BaseSettings {

    public String getLegacyMsiEndpoint() {
        return get(Source.DATABASE, "legacyMsiEndpoint", "http://msi.dma.dk/msi/ws/warning");
    }

}
