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

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

/**
 * Produces an injectable Auditor bean
 */
public class AuditorFactory {

    @Inject
    private AuditService auditService;

    /**
     * Factory method that produces the {@code Auditor} injection point
     * @param injectionPoint the CDI injection point
     * @return the new Auditor
     */
    @Produces
    public Auditor getAuditor(final InjectionPoint injectionPoint) {
        final String module = injectionPoint.getMember().getDeclaringClass().getName();
        return new Auditor() {

            public void info(String message, Object... args) {
                auditService.info(module, format(message, args));
            }

            public void error(String message, Object... args) {
                auditService.error(module, format(message, args));
            }

            public void error(Throwable exception, String message, Object... args) {
                auditService.error(module, format(message, args), exception);
            }
        };
    }

    /**
     * If the arguments are defined, the string is formatted with the arguments
     * using String.format()
     *
     * @param message the message
     * @param args the arguments
     * @return the formatted message
     */
    private String format(String message, Object... args) {
        return args != null && args.length > 0 ? String.format(message, args) : message;
    }
}
