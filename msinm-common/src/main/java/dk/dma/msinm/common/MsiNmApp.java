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
package dk.dma.msinm.common;

import dk.dma.msinm.common.settings.DefaultSetting;
import dk.dma.msinm.common.settings.Setting;
import dk.dma.msinm.common.settings.Settings;

import javax.ejb.DependsOn;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.inject.Inject;
import java.util.Locale;

/**
 * Common settings and functionality for the MsiNm app
 */
@Singleton
@Lock(LockType.READ)
@DependsOn("Settings")
public class MsiNmApp {

    private final static Setting LANGUAGES      = new DefaultSetting("languages", "en");
    private final static Setting ORGANIZATION   = new DefaultSetting("organization", "N/A");
    private final static Setting BASE_URI       = new DefaultSetting("baseUri", "http://localhost:8080");

    @Inject
    Settings settings;

    /**
     * Returns the list of languages supported by the MSI-NM application
     * @return the list of languages supported by the MSI-NM application
     */
    public String[] getLanguages() {
        String[] languages = settings.getArray(LANGUAGES);
        return (languages == null || languages.length == 0) ? new String[]{"en"} : languages;
    }

    /**
     * Returns the default language
     * @return the default language
     */
    public String getDefaultLanguage() {
        return getLanguages()[0];
    }

    /**
     * Ensures that the given language is a supported language and
     * returns the default language if not
     * @param lang the language to check
     * @return the language if supported, otherwise the default language
     */
    public String getLanguage(String lang) {
        for (String l : getLanguages()) {
            if (l.equalsIgnoreCase(lang)) {
                return l;
            }
        }
        return getDefaultLanguage();
    }

    /**
     * Ensures that the given language is a supported language and
     * returns the default locale if not
     * @param lang the language to check
     * @return the associated locale if supported, otherwise the default locale
     */
    public Locale getLocale(String lang) {
        return new Locale(getLanguage(lang));
    }

    /**
     * Returns the organization running the MSI-NM system
     * @return the organization running the MSI-NM system
     */
    public String getOrganization() {
        return settings.get(ORGANIZATION);
    }

    /**
     * Returns the base URI used to access this application
     * @return the base URI used to access this application
     */
    public String getBaseUri() {
        return settings.get(BASE_URI);
    }
}
