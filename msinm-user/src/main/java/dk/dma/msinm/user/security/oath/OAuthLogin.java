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
package dk.dma.msinm.user.security.oath;

import dk.dma.msinm.common.model.VersionedEntity;
import dk.dma.msinm.user.User;

import javax.persistence.*;

/**
 * Represents an OAuth login for a specific User
 * <p>
 * Based largely on: http://www.literak.cz/OAuthLogin/
 */
@Entity
@Table(name = "oauth_login")
@NamedQueries(value = {
        @NamedQuery(
                name="OAuthLogin:findLoginByProvider",
                query="SELECT OBJECT(login) FROM OAuthLogin login WHERE login.provider = :provider AND login.providerId = :providerId"
        ),
        @NamedQuery(
                name="OAuthLogin:findLoginByAccessToken",
                query="SELECT OBJECT(login) FROM OAuthLogin login WHERE login.provider = :provider AND login.accessToken = :accessToken"
        )
})
public class OAuthLogin extends VersionedEntity<Integer> {

    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    User user;

    @Column(name = "provider_code", nullable = false, length = 2)
    String provider;

    @Column(name = "provider_id", nullable = false, length = 255)
    String providerId;

    @Column(name = "access_token", nullable = true, length = 255)
    String accessToken;

    public OAuthLogin() {
    }

    public OAuthLogin(User user, String provider, String providerId) {
        this.user = user;
        setProvider(provider);
        this.providerId = providerId;
        user.getOauthLogins().add(this);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
