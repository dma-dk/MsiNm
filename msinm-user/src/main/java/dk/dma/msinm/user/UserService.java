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
package dk.dma.msinm.user;

import dk.dma.msinm.common.service.BaseService;
import dk.dma.msinm.user.security.oath.OAuthLogin;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;

/**
 * Business interface for managing User entities
 */
@Stateless
public class UserService extends BaseService {

    @Inject
    private Logger log;

    /**
     * Looks up an {@code OAuthLogin} for the given provider and id
     *
     * @param provider the provide
     * @param id the id
     * @return the matching entity or null
     */
    public OAuthLogin findByProvider(String provider, String id) {
        try {
            return em.createNamedQuery("OAuthLogin:findLoginByProvider", OAuthLogin.class)
                .setParameter("provider", provider)
                .setParameter("providerId", id)
                .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Looks up an {@code OAuthLogin} for the given provider and access token
     *
     * @param provider the provide
     * @param accessToken the access token
     * @return the matching entity or null
     */
    public OAuthLogin findByAccessToken(String provider, String accessToken) {
        try {
            return em.createNamedQuery("OAuthLogin:findLoginByAccessToken", OAuthLogin.class)
                .setParameter("provider", provider)
                .setParameter("accessToken", accessToken)
                .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }



}
