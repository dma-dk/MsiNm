package dk.dma.msinm.common.settings;

import dk.dma.msinm.common.cache.CacheElement;
import dk.dma.msinm.common.config.MsiNm;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

/**
 * Abstract base class for all settings.
 * <p>
 * Sub-classes should be annotated as stateless session beans or singletons.
 */
public class BaseSettings {

    private final  static  String SETTINGS_FILE = "/settings.properties";

    /**
     * The source of the setting can be database or system property
     */
    public enum Source {
        DATABASE,
        SYSTEM_PROPERTY
    }

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
                SettingsEntity result = new SettingsEntity();
                result.setKey(name);
                result.setValue(properties.getProperty(name));
                em.persist(result);
                log.info(String.format("Loaded property %s=%s from %s", name, properties.getProperty(name), SETTINGS_FILE));
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    /**
     * Returns the value associated with the key.
     * If it does not exist, it is created
     *
     * @param source the source
     * @param key the key
     * @param defaultValue the default value
     * @return the associated value
     */
    @Lock(LockType.READ)
    protected String get(Source source, String key, String defaultValue) {
        Objects.requireNonNull(key, "Must specify non-null cache key");

        // Look for a cached value
        CacheElement<String> value = settingsCache.getCache().get(key);

        // No cached value
        if (value == null) {
            // Either load from database or System property
            if (source == Source.DATABASE) {
                SettingsEntity result = em.find(SettingsEntity.class, key);
                if (result == null) {
                    result = new SettingsEntity();
                    result.setKey(key);
                    result.setValue(defaultValue);
                    em.persist(result);
                }
                value = new CacheElement<>(result.getValue());

            } else {
                // Tied to a system property
                value = new CacheElement<>(System.getProperty(key, defaultValue));
            }

            // Cache it. NB: We cannot cache null, so use a placeholder constant
            settingsCache.getCache().put(key, value);
        }

        return value.getElement();
    }

    /**
     * Returns the setting as a boolean
     *
     * @param source the source
     * @param key the key
     * @param defaultValue the default value
     * @return the associated value
     */
    protected boolean getBoolean(Source source, String key, boolean defaultValue) {
        String value = get(source, key, String.valueOf(defaultValue));
        switch(value.toLowerCase()) {
            case "true": case "yes": case "t" : case "y":
                return true;
        }
        return false;
    }

    /**
     * Returns the setting as a long
     *
     * @param source the source
     * @param key the key
     * @param defaultValue the default value
     * @return the associated value
     */
    protected long getLong(Source source, String key, long defaultValue) {
        String value = get(source, key, String.valueOf(defaultValue));
        return Long.valueOf(value);
    }

    /**
     * Returns the setting as a Path
     *
     * @param source the source
     * @param key the key
     * @param defaultValue the default value
     * @return the associated value
     */
    protected Path getPath(Source source, String key, String defaultValue) {
        String value = get(source, key, defaultValue);
        return Paths.get(value);
    }
}
