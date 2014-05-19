package dk.dma.msinm.user.security;

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
}
