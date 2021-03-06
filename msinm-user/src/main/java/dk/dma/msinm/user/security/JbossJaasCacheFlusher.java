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

import org.slf4j.Logger;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * Controlling the JBoss JAAS cache is somewhat problematic. Please refer to:
 * https://community.jboss.org/thread/203527?tstart=0
 * <p>
 * The {@linkplain dk.dma.msinm.user.security.JbossLoginModule} security module check for JWT expiry,
 * so we don't really want to cache the JAAS credentials.
 * <p>
 * This class will hook up a timer service and flush the JAAS cache every 1. minute.
 */
@Singleton
@Startup
public class JbossJaasCacheFlusher {

    public static final String SECURITY_DOMAIN = "jboss.as:subsystem=security,security-domain=msinm-policy";

    @Inject
    private Logger log;

    /**
     * Called every minute to flush the JAAS cache
     */
    //@Schedule(persistent = false, second = "27", minute = "*/1", hour = "*", dayOfWeek = "*", year = "*")
    private void flushJaasCache() {
        try {
            log.info("Flushing Jboss JAAS cache");
            MBeanServerConnection mbeanServerConnection = ManagementFactory.getPlatformMBeanServer();
            ObjectName mbeanName = new ObjectName(SECURITY_DOMAIN);
            mbeanServerConnection.invoke(mbeanName, "flushCache", null, null);
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    /**
     * Flush the cache for a specific user name
     * @param jaasUsername the JAAS user name
     */
    public void flushJaasCache(String jaasUsername) {
        try {
            Object[] params = { jaasUsername };
            String[] signature = { "java.lang.String" };

            log.info("Flushing Jboss JAAS cache for " + jaasUsername);
            MBeanServerConnection mbeanServerConnection = ManagementFactory.getPlatformMBeanServer();
            ObjectName mbeanName = new ObjectName(SECURITY_DOMAIN);
            mbeanServerConnection.invoke(mbeanName, "flushCache", params, signature);
        } catch (Exception e) {
            // Ignore
        }
    }
}
