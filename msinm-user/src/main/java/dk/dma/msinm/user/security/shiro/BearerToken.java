package dk.dma.msinm.user.security.shiro;

import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * A token class that handles JWT bearer tokens
 */
public class BearerToken extends UsernamePasswordToken {

    /**
     * Constructs a new RestAuthenticationToken encapsulating the
     * usual UsernamePasswordToken attributes, along with the curren site
     *
     * @param username   the username submitted for authentication
     * @param token      the token string submitted for authentication
     * @param host       the host name or IP string from where the attempt is occuring
     */
    public BearerToken(String username, String token, String host) {
        super(username, token, host);
    }

    public String getToken() {
        return String.valueOf(getPassword());
    }
}
