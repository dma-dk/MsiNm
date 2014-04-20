package dk.dma.msinm.legacy;

import dk.dma.msinm.common.config.LogConfiguration;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import javax.inject.Inject;

/**
 * Tests the {@linkplain LegacyMsiService} class
 */
@RunWith(CdiRunner.class)
@AdditionalClasses(value = { LegacyMsiService.class, LogConfiguration.class })
public class LegacyMsiServiceTest {

    @Inject
    Logger log;

    @Inject
    LegacyMsiService msiService;

    @Test
    public void test() {

        // Point to the test service
        //msiService.endpoint = "http://msi-beta.e-navigation.net/msi/ws/warning";
        msiService.countries = "DK";

        log.info(String.format("Fetched %d legacy MSI warnings from endpoint %s and country %s",
                msiService.getActiveWarnings().size(), msiService.endpoint, msiService.countries));
    }

}
