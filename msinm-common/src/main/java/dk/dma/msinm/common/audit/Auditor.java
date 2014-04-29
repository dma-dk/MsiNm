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

/**
 * An injectable Auditor bean that can be used to log audit entries
 * for the class the Auditor is injected into
 */
public interface Auditor {
    /**
     * Logs a new audit info message
     *
     * @param message the message
     * @param args optional list of arguments
     */
    public void info(String message, Object... args);

    /**
     * Logs a new audit error message
     *
     * @param message the message
     * @param args optional list of arguments
     */
    public void error(String message, Object... args);

    /**
     * Logs a new audit error message
     *
     * @param exception the exception
     * @param message the message
     * @param args optional list of arguments
     */
    public void error(Throwable exception, String message, Object... args);

}
