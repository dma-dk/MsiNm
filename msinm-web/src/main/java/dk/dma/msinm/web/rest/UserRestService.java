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
import dk.dma.msinm.user.UserVo;
import dk.dma.msinm.user.security.SecurityServletFilter;
import org.apache.commons.lang.StringUtils;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

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
     * Searches for users matching the given term
     * @param term the search term
     * @param limit the maximum number of results
     * @return the search result
     */
    @GET
    @Path("/search")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    @RolesAllowed({ "admin" })
    public List<UserVo> searchCharts(@QueryParam("term") String term, @QueryParam("limit") int limit) {
        log.info(String.format("Searching for users term='%s', limit=%d", term, limit));
        return userService.searchUsers(term, limit);
    }

    /**
     * Returns all users
     */
    @GET
    @Path("/all")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    @RolesAllowed({ "admin" })
    public List<UserVo> getUsers() {

        List<UserVo> users = new ArrayList<>();
        userService.getAll(User.class).forEach(user -> users.add(new UserVo(user)));
        return users;
    }

    /**
     * Returns the current user
     */
    @GET
    @Path("/current-user")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    @RolesAllowed({ "user" })
    public UserVo getCurrentUser() {
        User user = userService.getCurrentUser();
        return user == null ? null : new UserVo(user);
    }

    /**
     * Updates the current user details
     */
    @PUT
    @Path("/current-user")
    @Consumes("application/json")
    @Produces("application/json")
    @RolesAllowed({ "user" })
    public String updateCurrentUser(UserVo userVo) throws Exception {
        try {
            log.info(String.format("Updaring user email=%s, firstName=%s, lastName=%s", userVo.getEmail(), userVo.getFirstName(), userVo.getLastName()));
            User user = userVo.toEntity();
            userService.updateCurrentUser(user);
        } catch (Exception e) {
            throw new WebApplicationException(Response.serverError().type(MediaType.APPLICATION_JSON).entity(e.getMessage()).build());
        }
        return "User " + userVo.getEmail() + " updated.";
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
            User user = userVo.toEntity();
            userService.registerUser(user);
        } catch (Exception e) {
            throw new WebApplicationException(Response.serverError().type(MediaType.APPLICATION_JSON).entity(e.getMessage()).build());
        }
        return "User " + userVo.getEmail() + " registered.";
    }

    @POST
    @Path("/create-or-update-user")
    @Consumes("application/json")
    @Produces("application/json")
    @RolesAllowed({ "admin" })
    public String createOrUpdateUser(UserVo userVo) throws Exception {
        try {
            log.info(String.format("Create/update user email=%s, firstName=%s, lastName=%s", userVo.getEmail(), userVo.getFirstName(), userVo.getLastName()));
            User user = userVo.toEntity();
            List<String> roles = userVo.getRoles();
            userService.createOrUpdateUser(user, roles.toArray(new String[roles.size()]), userVo.getActivationEmail());
        } catch (Exception e) {
            throw new WebApplicationException(Response.serverError().type(MediaType.APPLICATION_JSON).entity(e.getMessage()).build());
        }
        return "User " + userVo.getEmail() + " created/updated.";
    }

    /**
     * Checks if the current user has the given role.
     * Will return:
     * <ul>
     *     <li>'true': If the user has the given role</li>
     *     <li>'false': If the user does not have the given role</li>
     *     <li>'error': If user authentication failed</li>
     * </ul>
     */
    @GET
    @Path("/check-role/{role}")
    @Produces("application/json")
    @NoCache
    public String checkRole(@PathParam("role") String role, @Context HttpServletRequest request) {
        if (StringUtils.isBlank(role)) {
            return String.valueOf(true);
        }

        User user = userService.getCurrentUser();
        if (user == null) {
            // Check if there was an authentication error, e.g. if the JWT token has expired
            if (request.getAttribute(SecurityServletFilter.AUTH_ERROR_ATTR) != null) {
                return "error";
            }
            // User just not authenticated
            return String.valueOf(false);
        }

        // User authenticated - check role
        return String.valueOf(user.hasRole(role));
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

}
