package dk.dma.msinm.user.security;

import org.apache.commons.lang.StringUtils;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

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
     * @param request the request
     * @return the parsed credentials or null
     */
    public static Credentials fromRequest(HttpServletRequest request) {
        Credentials credentials = new Credentials();
        try (JsonReader jsonReader = Json.createReader(request.getReader())) {
            JsonObject jsonObject = jsonReader.readObject();
            credentials.setEmail(jsonObject.getString("email"));
            credentials.setPassword(jsonObject.getString("password"));
        } catch (IOException e) {
            // No valid credentials
        }
        return (credentials.isValid()) ? credentials : null;
    }

}
