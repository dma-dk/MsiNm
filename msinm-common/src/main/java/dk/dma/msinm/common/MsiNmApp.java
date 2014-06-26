package dk.dma.msinm.common;

import dk.dma.msinm.common.settings.annotation.Setting;

import javax.ejb.DependsOn;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.inject.Inject;

/**
 * Common settings and functionality for the MsiNm app
 */
@Singleton
@Lock(LockType.READ)
@DependsOn("Settings")
public class MsiNmApp {

    @Inject
    @Setting(value = "languages", defaultValue = "en")
    String[] languages;

    /**
     * Returns the list of languages supported by the MSI-NM application
     * @return the list of languages supported by the MSI-NM application
     */
    public String[] getLanguages() {
        return (languages == null || languages.length == 0) ? new String[] { "en" } : languages;
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
}
