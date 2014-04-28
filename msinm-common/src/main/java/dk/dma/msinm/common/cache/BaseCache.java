package dk.dma.msinm.common.cache;

import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.CacheContainer;
import org.infinispan.manager.DefaultCacheManager;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

/**
 * Base class for Infinispan caches
 */
public abstract class BaseCache {

    @Inject
    private Logger log;

    protected CacheContainer cacheContainer;

    /**
     * Starts the cache container
     */
    @PostConstruct
    public void initCacheContainer() {
        if (cacheContainer == null) {
            GlobalConfiguration globalConfiguration = new GlobalConfigurationBuilder()
                    .nonClusteredDefault() //Helper method that gets you a default constructed GlobalConfiguration, preconfigured for use in LOCAL mode
                    .globalJmxStatistics().enable() //This method allows enables the jmx statistics of the global configuration.
                    .build(); //Builds  the GlobalConfiguration object
            Configuration localConfiguration = createCacheConfiguration();
            cacheContainer = new DefaultCacheManager(globalConfiguration, localConfiguration, true);
            log.info("Stopped cache container");
        }
    }

    /**
     * Must be implemented by sub-classes to define the local cache configuration
     * @return the local cache configuration
     */
    protected abstract Configuration createCacheConfiguration();

    /**
     * Stops the cache container
     */
    @PreDestroy
    public void destroyCacheContainer() {
        if (cacheContainer != null) {
            cacheContainer.stop();
            cacheContainer = null;
            log.info("Stopped cache container");
        }
    }
}
