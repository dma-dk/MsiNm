package dk.dma.msinm.common;

import dk.dma.msinm.common.config.LogConfiguration;
import dk.dma.msinm.common.db.SqlProducer;
import dk.dma.msinm.common.light.LightService;
import dk.dma.msinm.common.settings.Settings;
import dk.dma.msinm.common.settings.SettingsEntity;
import dk.dma.msinm.test.MsiNmUnitTest;
import dk.dma.msinm.test.TestResources;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for the light parser service
 */
@RunWith(CdiRunner.class)
@AdditionalClasses(value = {
        TestResources.class, SqlProducer.class, Settings.class, LogConfiguration.class, EntityManager.class
})
public class LightTest extends MsiNmUnitTest {

    @BeforeClass
    public static void prepareEntityManagerFactory() throws ClassNotFoundException {
        prepareEntityManagerFactory(
                SettingsEntity.class
        );
    }

    @Inject
    LightService lightService;

    List<String> lights = Arrays.asList(
            "Fl(2) 20m 10s 12M ",
            "Gp.L.Fl(2+1)G. 5s",
            "Mo(U)",
            "Iso G 4s",
            "Gp Oc(3) W 10s 15m 10M",
            "Alt R.W.G",
            "VQ(6)+LFl"
        );

    @Test
    public void lightModelTest() throws Exception {

        lights.forEach(l -> System.out.printf("%s  ->  %s%n", l, lightService.parse(l)));
    }


    @Test
    public void lightParserTestEn() throws Exception {

       lights.forEach(l -> {
                try {
                    System.out.printf("%s  ->  %s%n", l, lightService.parse("en", l));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
        });
    }

    @Test
    public void lightParserTestDa() throws Exception {

        lights.forEach(l -> {
            try {
                System.out.printf("%s  ->  %s%n", l, lightService.parse("da", l));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

}
