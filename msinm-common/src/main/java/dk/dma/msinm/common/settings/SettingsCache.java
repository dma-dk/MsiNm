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
