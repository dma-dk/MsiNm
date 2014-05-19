package dk.dma.msinm.user.security;

import dk.dma.msinm.user.User;
import dk.dma.msinm.user.UserService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

/**
 * Functionality for handling user login and encrypting passwords
 */
public class SecurityUtils {

    /**
     * Attempts to log-in the user.
     * <p>
     * The web-app is using a custom login-module, {@linkplain JbossLoginModule}, and the
     * natural solution would be that this module set the {@code User} as the user principal
     * upon successful authentication.
     * <br>
     * However, this tends to cause ClassCastException's when the web-app has been reloaded,
     * because a different class-loader is used for the login-modules.
     * <br>
     * Hence, the login-module sets a {@code SimplePrincipal} as the request user principal, and this
     * method swaps the {@code SimplePrincipal} for a {@code User} principal.
     *
     * @param userService the user service
     * @param request the servlet request
     * @param username the user name
     * @param password the password
     * @return the updated request
     */
    public static HttpServletRequest login(UserService userService, HttpServletRequest request, String username, String password) throws ServletException {
        // Will throw an exception if the login fails
        request.login(username, password);

        String id = request.getUserPrincipal().getName();
        final User user = userService.getByPrimaryKey(User.class, Integer.valueOf(id));
        return new HttpServletRequestWrapper(request) {
            @Override
            public java.security.Principal getUserPrincipal() {
                return user;
            }
        };
    }

    /**
     * Hashes and Hex'es the password
     * @param pwd the password
     * @param type the digest, e.g. "SHA-512"
     * @return the hashed and hex'ed password
     */
    public static String encrypt(String pwd, String type) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(type);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to create message digest", e);
        }

        byte[] val = md.digest(pwd.getBytes());

        StringBuilder sb = new StringBuilder(val.length * 2);

        Formatter formatter = new Formatter(sb);
        for (byte b : val) {
            formatter.format("%02x", b);
        }

        return sb.toString();
    }

}
