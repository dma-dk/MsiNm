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

/**
 * Interface that must be implemented by settings
 */
public interface Setting {

    /**
     * Returns the name of the setting
     * @return the name of the setting
     */
    String getSettingName();

    /**
     * Returns the default setting value
     * @return the default setting value
     */
    public default String defaultValue() {
        return "";
    }

    /**
     * Returns the source
     * @return the source
     */
    public default Source getSource() {
        return Source.DATABASE;
    }

    /**
     * Returns an optional non-default cache timeout in seconds.
     * If null is returned, the default value is used.
     * @return the optional non-default cache timeout in seconds.
     */
    public default Long getCacheTimeout() { return null; }

    /**
     * Returns whether to substitute system properties or not.
     * If true, segments of the form "${prop}" will be replaced
     * with the corresponding system property.
     * @return whether to substitute system properties or not
     */
    public default boolean substituteSystemProperties() { return false; }
}
