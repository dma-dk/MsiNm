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
package dk.dma.msinm.web.rest;

import dk.dma.msinm.user.User;
import dk.dma.msinm.user.security.JWTService;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * REST interface for accessing MSI-NM messages
 */
@Path("/user")
@Stateless
public class UserRestService {

    @Inject
    Logger log;

    @Inject
    JWTService jwtService;

    /**
     * Authenticates the user and throws an error if the login fails
     *
     * @param credentials the user credentials
     * @return the user JWT token
     */
    @POST
    @Path("/auth")
    @Consumes("application/json")
    @Produces("application/json")
    @NoCache
    public JWTToken authenticate(@Context HttpServletRequest request, Credentials credentials) {
        log.info("Login by " + credentials.getUsername());

        try {
            if ("a".equals(credentials.getUsername()) && "a".equals(credentials.getPassword())) {
                String svr = String.format("%s://%s", request.getScheme(), request.getServerName());

                JWTToken token = new JWTToken();
                User user = new User();
                user.setId(1);
                token.setToken(jwtService.getSignedJWT(svr, user));
                token.setRoles(new String[] {"user", "admin"});
                return token;
            }
        } catch (Exception e) {
            log.error("Failed generating JWT for user " + credentials.getUsername(), e);
        }

        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    @GET
    @Path("/test")
    @Produces("application/json")
    @NoCache
    public String test() {
        return "SUCCESS";
    }

    /**
     * User credentials
     */
    public static class Credentials {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * JWT token
     */
    public static class JWTToken {
        private String token;
        private String[] roles;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String[] getRoles() {
            return roles;
        }

        public void setRoles(String[] roles) {
            this.roles = roles;
        }
    }
}