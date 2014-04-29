package dk.dma.msinm.common.settings;

import dk.dma.msinm.common.cache.CacheElement;
import dk.dma.msinm.common.config.MsiNm;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

import static dk.dma.msinm.common.settings.Setting.Source.DATABASE;

/**
 * Interface for accessing settings
 * <p>
 * Sub-classes should be annotated as stateless session beans or singletons.
 */
@Singleton
public class Settings {

    private final  static  String SETTINGS_FILE = "/settings.properties";

    @Inject
    private Logger log;

    @Inject
    @MsiNm
    protected EntityManager em;

    @Inject
    SettingsCache settingsCache;

    @PostConstruct
    public void loadSettingsFromPropertiesFile() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream(SETTINGS_FILE));
            for (String name : properties.stringPropertyNames()) {
                SettingsEntity result = new SettingsEntity(name, properties.getProperty(name));
                em.persist(result);
                log.info(String.format("Loaded property %s=%s from %s", name, properties.getProperty(name), SETTINGS_FILE));
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    /**
     * Returns the value associated with the setting.
     * If it does not exist, it is created
     *
     * @param setting the source
     * @return the associated value
     */
    @Lock(LockType.READ)
    public String get(Setting setting) {
        Objects.requireNonNull(setting, "Must specify valid setting");

        // Look for a cached value
        CacheElement<String> value = settingsCache.getCache().get(setting.getSettingName());

        // No cached value
        if (value == null) {
            // Either load from database or System property
            if (setting.getSource() == DATABASE) {
                SettingsEntity result = em.find(SettingsEntity.class, setting.getSettingName());
                if (result == null) {
                    result = new SettingsEntity(setting);
                    em.persist(result);
                }
                value = new CacheElement<>(result.getValue());

            } else {
                // Tied to a system property
                value = new CacheElement<>(System.getProperty(setting.getSettingName(), setting.defaultValue()));
            }

            // Cache it. NB: We cannot cache null, so use a placeholder constant
            settingsCache.getCache().put(setting.getSettingName(), value);
        }

        return value.getElement();
    }

    /**
     * Returns the setting as a boolean
     *
     * @param setting the source
     * @return the associated value
     */
    protected boolean getBoolean(Setting setting) {
        String value = get(setting);
        switch(value.toLowerCase()) {
            case "true": case "yes": case "t" : case "y":
                return true;
        }
        return false;
    }

    /**
     * Returns the setting as a long
     *
     * @param setting the source
     * @return the associated value
     */
    protected long getLong(Setting setting) {
        String value = get(setting);
        return Long.valueOf(value);
    }

    /**
     * Returns the setting as a Path
     *
     * @param setting the source
     * @return the associated value
     */
    protected Path getPath(Setting setting) {
        String value = get(setting);
        return Paths.get(value);
    }
}
