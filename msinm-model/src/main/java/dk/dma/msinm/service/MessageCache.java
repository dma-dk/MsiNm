package dk.dma.msinm.service;

import dk.dma.msinm.common.cache.BaseCache;
import dk.dma.msinm.model.Message;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.util.concurrent.IsolationLevel;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Implements the message cache with a default timeout of 10 minutes
 */
@ApplicationScoped
public class MessageCache extends BaseCache {

    final static long LIFESPAN = 10 * 60 * 1000; // 10 minutes
    final static String CACHE_ID = "messageCache";

    @Inject
    private Logger log;

    /**
     * Returns a reference to the settings cache
     * @return a reference to the settings cache
     */
    public Cache<Integer, Message> getCache() {
        return cacheContainer.getCache(CACHE_ID);
    }

    /**
     * Clears the cache
     */
    @Override
    public  void clearCache() {
        log.info("Clearing cache " + CACHE_ID);
        getCache().clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Configuration createCacheConfiguration() {
        return new ConfigurationBuilder()
                .clustering().cacheMode(CacheMode.LOCAL)
                .locking().isolationLevel(IsolationLevel.REPEATABLE_READ)
                .eviction().maxEntries(20000).strategy(EvictionStrategy.LRU)
                .expiration().lifespan(LIFESPAN)
                .build();
    }

}
