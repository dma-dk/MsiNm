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

import dk.dma.msinm.user.User;
import dk.dma.msinm.user.UserService;
import org.apache.commons.lang.StringUtils;
import org.jboss.security.SimplePrincipal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.security.MessageDigest;
import java.security.Principal;
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
        //request.logout();
        request.login(username, password);

        // The email is used as it is unique for the user
        String email = request.getUserPrincipal().getName();
        final User user = userService.findByEmail(email);
        return new HttpServletRequestWrapper(request) {
            @Override
            public java.security.Principal getUserPrincipal() {
                return user;
            }
        };
    }

    /**
     * See the comment for {@code login()}. The {@linkplain JbossLoginModule} returns a {@code SimplePrincipal}
     * rather than a {@code User}, which is then swapped back by the {@linkplain dk.dma.msinm.user.security.SecurityServletFilter}
     * @param user the user to return a SimplePrincipal for
     * @return the principal for the user
     */
    public static Principal getPrincipal(User user) {
        // The email is used as it is unique for the user
        return new SimplePrincipal(user.getEmail());
    }

    /**
     * Hashes and Hex'es the password
     * @param pwd the password
     * @param salt the salt
     * @param type the digest, e.g. "SHA-512"
     * @return the hashed and hex'ed password
     */
    public static String encrypt(String pwd, String salt, String type) {
        try {
            MessageDigest md = MessageDigest.getInstance(type);
            md.reset();
            if (StringUtils.isNotBlank(salt)) {
                md.update(salt.getBytes("UTF-8"));
            }
            md.update(pwd.getBytes("UTF-8"));
            byte[] hash = md.digest();

            return hex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Unable to create message digest", e);
        }
    }

    /**
     * Hashes and Hex'es the password
     * @param pwd the password
     * @param type the digest, e.g. "SHA-512"
     * @return the hashed and hex'ed password
     */
    public static String encrypt(String pwd, String type) {
        return encrypt(pwd, null, type);

    }

    /**
     * Hex the byte buffer
     * @param bytes the bytes to Hex
     * @return the result
     */
    public static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);

        Formatter formatter = new Formatter(sb);
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }

        return sb.toString();
    }
}
