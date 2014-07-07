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
package dk.dma.msinm.common.mail;

import dk.dma.msinm.common.cache.BaseCache;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.util.concurrent.IsolationLevel;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.URL;

/**
 * Implements the mail attachment cache with a default timeout of 5 minutes.
 * <p></p>
 * If you compose a mime message with attachments by
 * setting "MimeBodyPart.setDataHandler(new DataHandler(url))",
 * then the url will actually be fetched 2-3 times during
 * the process of sending the mail by javamail!!
 * <p></p>
 * To alleviate this sickening behavior, this {@code CachedUrlDataSource} is
 * be used to wrap the original data source.
 * The content is only loaded once and then cached in this cache.
 *
 */
@ApplicationScoped
public class MailAttachmentCache extends BaseCache {

    final static long LIFESPAN = 5 * 60 * 1000; // 5 minutes
    final static String CACHE_ID = "mailAttachmentCache";

    @Inject
    private Logger log;

    /**
     * Returns a reference to the mail attachment cache
     * @return a reference to the mail attachment cache
     */
    public Cache<URL, CachedUrlData> getCache() {
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
                .expiration().lifespan(LIFESPAN)
                .build();
    }
}
