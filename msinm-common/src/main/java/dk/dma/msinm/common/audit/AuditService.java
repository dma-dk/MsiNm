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
package dk.dma.msinm.common.audit;

import dk.dma.msinm.common.service.BaseService;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Future;

/**
 * Provides business interface for logging and querying audit entries
 */
@Stateless
public class AuditService extends BaseService {

    @Inject
    private Logger log;

    /**
     * Asynchronous method for logging a new audit entry
     *
     * @param level the audit level
     * @param module the module
     * @param message the message
     * @param exception optionally, an exception
     */
    @Asynchronous
    public Future<AuditEntry> log(AuditEntry.Level level, String module, String message, Throwable exception) {
        AuditEntry entry = new AuditEntry();
        entry.setCreated(DateTime.now(DateTimeZone.UTC));
        entry.setMessage(StringUtils.abbreviate(message, 200));
        entry.setModule(module);
        entry.setLevel(level);

        if (exception != null) {
            StringWriter sw = new StringWriter();
            exception.printStackTrace(new PrintWriter(sw));
            entry.setStackTrace(StringUtils.abbreviate(sw.toString(), 4000));
        }

        AuditEntry result = saveEntity(entry);
        log.debug("Saved a new audit log " + result);
        return new AsyncResult<>(result);
    }

    /**
     * Logs a new audit info message
     *
     * @param module the module
     * @param message the message
     */
    public void info(String module, String message) {
        log(AuditEntry.Level.OK, module, message, null);
    }

    /**
     * Logs a new audit error message
     *
     * @param module the module
     * @param message the message
     */
    public void error(String module, String message) {
        log(AuditEntry.Level.ERROR, module, message, null);
    }

    /**
     * Logs a new audit error message
     *
     * @param module the module
     * @param message the message
     * @param exception the exception
     */
    public void error(String module, String message, Throwable exception) {
        log(AuditEntry.Level.ERROR, module, message, exception);
    }
}
