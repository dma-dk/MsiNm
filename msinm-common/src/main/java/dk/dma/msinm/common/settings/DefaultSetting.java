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

import java.util.Objects;

/**
 * Default implementation of a setting
 */
public class DefaultSetting implements Setting {

    String settingName;
    String defaultValue;
    Source source;
    Long cacheTimeout;
    boolean substituteSystemProperties;

    /**
     * Designated constructor
     */
    public DefaultSetting(String settingName, String defaultValue, Source source, Long cacheTimeout, boolean substituteSystemProperties) {
        Objects.requireNonNull(settingName);
        Objects.requireNonNull(source);

        this.settingName = settingName;
        this.defaultValue = defaultValue;
        this.source = source;
        this.cacheTimeout = cacheTimeout;
        this.substituteSystemProperties = substituteSystemProperties;
    }

    public DefaultSetting(String settingName, String defaultValue, Source source) {
        this(settingName, defaultValue, source, null, false);
    }

    public DefaultSetting(String settingName, String defaultValue) {
        this(settingName, defaultValue, Source.DATABASE);
    }

    public DefaultSetting(String settingName, Source source) {
        this(settingName, null, source);
    }

    public DefaultSetting(String settingName) {
        this(settingName, null, Source.DATABASE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSettingName() {
        return settingName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String defaultValue() {
        return defaultValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Source getSource() {
        return source;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getCacheTimeout() {
        return cacheTimeout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean substituteSystemProperties() {
        return substituteSystemProperties;
    }
}
