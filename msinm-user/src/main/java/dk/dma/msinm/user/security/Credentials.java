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

import org.apache.commons.lang.StringUtils;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.StringReader;

/**
 * User credentials
 */
public class Credentials {
    private String email;
    private String password;

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

    public boolean isValid() {
        return StringUtils.isNotBlank(email) && StringUtils.isNotBlank(password);
    }

    /**
     * Attempts to parse the request JSON payload as Credentials, and returns null if it fails
     * @param requestBody the request
     * @return the parsed credentials or null
     */
    public static Credentials fromRequest(String requestBody) {
        Credentials credentials = new Credentials();
        try (JsonReader jsonReader = Json.createReader(new StringReader(requestBody))) {
            JsonObject jsonObject = jsonReader.readObject();
            credentials.setEmail(jsonObject.getString("email"));
            credentials.setPassword(jsonObject.getString("password"));
        } catch (Exception e) {
            // No valid credentials
        }
        return (credentials.isValid()) ? credentials : null;
    }

}
