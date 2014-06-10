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
import dk.dma.msinm.user.UserService;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST interface for accessing MSI-NM users
 */
@Path("/user")
@Stateless
@SecurityDomain("msinm-policy")
@PermitAll
public class UserRestService {

    @Inject
    Logger log;

    @Inject
    UserService userService;

    /**
     * Returns all users
     */
    @GET
    @Path("/all")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    @RolesAllowed({ "admin" })
    public JsonArray getUsers() {

        JsonArrayBuilder result = Json.createArrayBuilder();
        userService.getAll(User.class).forEach(user -> result.add(user.toJson()));
        return result.build();
    }

    @POST
    @Path("/reset-password")
    @Consumes("application/json")
    @Produces("application/json")
    public String resetPassword(String email) {
        try {
            userService.resetPassword(email);
        } catch (Exception e) {
            throw new WebApplicationException(Response.serverError().type(MediaType.APPLICATION_JSON).entity(e.getMessage()).build());
        }
        return "A reset email has been sent";
    }

    @POST
    @Path("/update-password")
    @Consumes("application/json")
    @Produces("application/json")
    public String updatePassword(UpdatePasswordVo updatePasswordVo) throws Exception {
        try {
            log.info(String.format("Setting new password for email %s, token %s", updatePasswordVo.getEmail(), updatePasswordVo.getToken()));
            userService.updatePassword(updatePasswordVo.getEmail(), updatePasswordVo.getPassword(), updatePasswordVo.getToken());
        } catch (Exception e) {
            throw new WebApplicationException(Response.serverError().type(MediaType.APPLICATION_JSON).entity(e.getMessage()).build());
        }
        return "Password updated";
    }

    @POST
    @Path("/register-user")
    @Consumes("application/json")
    @Produces("application/json")
    public String registerUser(UserVo userVo) throws Exception {
        try {
            log.info(String.format("Registering user email=%s, firstName=%s, lastName=%s", userVo.getEmail(), userVo.getFirstName(), userVo.getLastName()));
            User user = new User();
            user.setEmail(userVo.getEmail());
            user.setFirstName(userVo.getFirstName());
            user.setLastName(userVo.getLastName());
            userService.registerUser(user, userVo.getPassword());
        } catch (Exception e) {
            throw new WebApplicationException(Response.serverError().type(MediaType.APPLICATION_JSON).entity(e.getMessage()).build());
        }
        return "User " + userVo.getEmail() + " created.";
    }


    /**
     * Helper class used for setting a new password
     */
    public static class UpdatePasswordVo {

        String email, password, token;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    /**
     * Helper class used for creating users
     */
    public static class UserVo {
        String email, firstName, lastName, password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
