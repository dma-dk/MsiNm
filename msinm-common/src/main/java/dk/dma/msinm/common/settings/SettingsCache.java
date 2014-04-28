package dk.dma.msinm.common.settings;

import dk.dma.msinm.common.cache.BaseCache;
import dk.dma.msinm.common.cache.CacheElement;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.util.concurrent.IsolationLevel;

import javax.enterprise.context.ApplicationScoped;

/**
 * Implements the settings cache with a default timeout of 1 minute
 */
@ApplicationScoped
public class SettingsCache extends BaseCache {

    final static long LIFESPAN = 60 * 1000; // 1 minute
    final static String CACHE_ID = "settingsCache";

    /**
     * Returns a reference to the settings cache
     * @return a reference to the settings cache
     */
    public Cache<String, CacheElement<String>> getCache() {
        return cacheContainer.getCache(CACHE_ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Configuration createCacheConfiguration() {
        return new ConfigurationBuilder()
                .clustering().cacheMode(CacheMode.LOCAL)
                .locking().isolationLevel(IsolationLevel.REPEATABLE_READ)
                .expiration().lifespan(LIFESPAN)
                .build();
    }
}
