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
package dk.dma.msinm.user.security;

import dk.dma.msinm.common.cache.BaseCache;
import dk.dma.msinm.common.cache.CacheElement;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.util.concurrent.IsolationLevel;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Implements the Auth token cache with a default timeout of 1 minute
 */
@ApplicationScoped
public class AuthCache extends BaseCache {

    public static final String AUTH_TOKEN_PREFIX = "auth_";

    final static long LIFESPAN = 60 * 1000; // 1 minute
    final static String CACHE_ID = "authCache";

    @Inject
    private Logger log;

    /**
     * Returns a reference to the settings cache
     * @return a reference to the settings cache
     */
    public Cache<String, JWTToken> getCache() {
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
        // TODO: This should be changed to use a clustered cache
        return new ConfigurationBuilder()
                .clustering().cacheMode(CacheMode.LOCAL)
                .locking().isolationLevel(IsolationLevel.REPEATABLE_READ)
                .expiration().lifespan(LIFESPAN)
                .build();
    }
}
