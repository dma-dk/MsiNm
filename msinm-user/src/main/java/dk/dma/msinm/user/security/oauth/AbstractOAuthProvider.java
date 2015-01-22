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
package dk.dma.msinm.user.security.oauth;

import dk.dma.msinm.common.settings.DefaultSetting;
import dk.dma.msinm.common.settings.Setting;
import dk.dma.msinm.common.settings.Settings;
import dk.dma.msinm.user.User;
import org.apache.commons.lang.StringUtils;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.Api;
import org.scribe.oauth.OAuthService;

import javax.servlet.http.HttpServletRequest;

/**
 * Abstract super class for OAuth providers
 */
public abstract class AbstractOAuthProvider {

    /**
     * Returns the OAuth provider ID
     * @return the OAuth provider ID
     */
    public abstract String getOAuthProviderId();

    /**
     * Returns the OAuth API implementation
     * @return the OAuth API implementation
     */
    public abstract Class<? extends Api> getOAuthApi();

    /**
     * Returns the OAuth scope
     * @return the OAuth scope
     */
    public abstract String getOAuthScope();

    /**
     * Returns the setting associated with the provider-specific OAuth API key
     * @return the setting associated with the provider-specific OAuth API key
     */
    public Setting getApiKeySetting() {
        return new DefaultSetting(getOAuthProviderId() + "OAuthApiKey");
    }

    /**
     * Returns the setting associated with the provider-specific OAuth API secret
     * @return the setting associated with the provider-specific OAuth API secret
     */
    public Setting getApiSecretSetting() {
        return new DefaultSetting(getOAuthProviderId() + "OAuthApiSecret");
    }

    /**
     * Returns an OAuth service instance and null if not supported
     * @param settings the settings service
     * @param baseUri the base URI of the MSI-NM application
     * @return an OAuth service instance and null if not supported
     */
    protected OAuthService getOAuthService(Settings settings, String baseUri) {

        String apiKey = settings.get(getApiKeySetting());
        String apiSecret = settings.get(getApiSecretSetting());

        if (StringUtils.isNotBlank(apiKey) && StringUtils.isNotBlank(apiSecret)) {
            return new ServiceBuilder()
                    .provider(getOAuthApi())
                    .apiKey(settings.get(getApiKeySetting()))
                    .apiSecret(settings.get(getApiSecretSetting()))
                    .scope(getOAuthScope())
                    .callback(baseUri + "/oauth/callback/" + getOAuthProviderId())
                    .build();
        }

        // No service
        return null;
    }

    /**
     * Create an authorization URL for this provider.
       If not possible, null is returned.
     * @return an authorization URL for this provider or null
     */
    public abstract String getAuthorizationUrl();

    /**
     * Lookup or create a user from an OAuth callback request
     * @param request the OAuth callback request
     * @return the user
     * @throws Exception in case of an error
     */
    public abstract User authenticateUser(HttpServletRequest request) throws Exception;

}
