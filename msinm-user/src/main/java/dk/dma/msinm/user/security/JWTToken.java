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
package dk.dma.msinm.user.security;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import java.util.Arrays;

/**
 * JWT token as returned to the client as a JSON object.
 * <p>
 * The {@code token} attributes contains the entire signed JWT token,
 * and the remaining attributes contain public information about the user.
 */
public class JWTToken {
    private String token;
    private String name;
    private String email;
    private String[] roles;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    /**
     * Returns a hand-crafted Json representation of the token
     * @return a hand-crafted Json representation of the token
     */
    public String toJson() {
        JsonArrayBuilder rolesJson = Json.createArrayBuilder();
        Arrays.asList(roles).forEach(rolesJson::add);
        return Json.createObjectBuilder()
                .add("token", getToken())
                .add("name", name)
                .add("email", email)
                .add("roles", rolesJson)
                .build()
                .toString();
    }
}
