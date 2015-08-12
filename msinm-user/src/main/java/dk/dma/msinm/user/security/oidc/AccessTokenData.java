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
package dk.dma.msinm.user.security.oidc;

import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import java.io.StringReader;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Used for parsed access token data
 */
@SuppressWarnings("unused")
public class AccessTokenData {

    String name;
    String userName;
    String email;
    String givenName;
    String familyName;
    Set<String> realmRoles = new HashSet<>();
    Set<String> resourceRoles = new HashSet<>();

    /** Parses the JWT access token and extract relevant data */
    public static AccessTokenData parseJWTToken(SignedJWT jwt, String resource) throws ParseException {
        AccessTokenData data = new AccessTokenData();

        ReadOnlyJWTClaimsSet claims = jwt.getJWTClaimsSet();
        Map<String, Object> customClaims =  claims.getCustomClaims();
        data.setName(toString(customClaims.get("name")));
        data.setUserName(toString(customClaims.get("preferred_username")));
        data.setEmail(toString(customClaims.get("email")));
        data.setGivenName(toString(customClaims.get("given_name")));
        data.setFamilyName(toString(customClaims.get("family_name")));

        // Parse realm roles
        if (customClaims.get("realm_access") != null) {
            try (JsonReader jr = Json.createReader(new StringReader(customClaims.get("realm_access").toString()))) {
                addRoles(data.getRealmRoles(), jr.readObject());
            }
        }

        // Parse resource roles
        if (customClaims.get("resource_access") != null) {
            try (JsonReader jr = Json.createReader(new StringReader(customClaims.get("resource_access").toString()))) {
                JsonObject json = jr.readObject();
                if (json.containsKey(resource)) {
                    addRoles(data.getResourceRoles(), json.getJsonObject(resource));
                }
            }
        }

        return data;
    }

    /** Extract the roles list from the JSON object */
    private static void addRoles(Set<String> rolesSet, JsonObject json) {
        if (json.containsKey("roles")) {
            JsonArray roles = json.getJsonArray("roles");
            for (JsonString role : roles.getValuesAs(JsonString.class)) {
                rolesSet.add(role.getString());
            }
        }
    }

    /** Returns a string representation of the access token */
    @Override
    public String toString() {
        return "AccessTokenData{" +
                "name='" + name + '\'' +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", givenName='" + givenName + '\'' +
                ", familyName='" + familyName + '\'' +
                ", realmRoles=" + realmRoles +
                ", resourceRoles=" + resourceRoles +
                '}';
    }

    // ** Getters and setters **

    private static String toString(Object obj) {
        return obj == null ? null : obj.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public Set<String> getRealmRoles() {
        return realmRoles;
    }

    public void setRealmRoles(Set<String> realmRoles) {
        this.realmRoles = realmRoles;
    }

    public Set<String> getResourceRoles() {
        return resourceRoles;
    }

    public void setResourceRoles(Set<String> resourceRoles) {
        this.resourceRoles = resourceRoles;
    }
}
