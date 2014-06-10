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
package dk.dma.msinm.user;

import dk.dma.msinm.common.mail.MailService;
import dk.dma.msinm.common.service.BaseService;
import dk.dma.msinm.user.security.JbossJaasCacheFlusher;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Business interface for managing User entities
 */
@Stateless
public class UserService extends BaseService {

    /** Require 6-20 characters, at least 1 digit and 1 character */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("((?=.*\\d)(?=.*[a-zA-ZæøåÆØÅ]).{6,20})");

    @Inject
    private Logger log;

    @Inject
    private MailService mailService;

    @Inject
    private JbossJaasCacheFlusher jbossJaasCacheFlusher;

    /**
     * Looks up the {@code User} with the given id and preloads the roles
     *
     * @param id the id
     * @return the user or null
     */
    public User findById(Integer id) {
        try {
            return em.createNamedQuery("User.findById", User.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Looks up the {@code User} with the given email addressand preloads the roles
     *
     * @param email the email
     * @return the user or null
     */
    public User findByEmail(String email) {
        try {
            return em.createNamedQuery("User.findByEmail", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Finds the role with the given name
     *
     * @param name the name of the role
     * @return the role or null if not found
     */
    public Role findRoleByName(String name) {
        try {
            return em.createNamedQuery("Role.findByName", Role.class)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Validates the strength of the password
     * @param password the password to validate
     * @return if the password is valid or not
     */
    public boolean validatePasswordStrength(String password) {
        if (password == null) {
            return false;
        }

        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Called when a person registers a new user via the website.
     * The user will automatically get the "user" role.
     *
     * @param user the template user entity
     * @param password the password of choice
     * @return the updated user
     */
    public User registerUser(User user, String password) throws Exception {
        // Validate that the email address is not already registered
        if (findByEmail(user.getEmail()) != null) {
            throw new Exception("Email " + user.getEmail() + " is already registered");
        }

        // Validate the password strength
        if (!validatePasswordStrength(password)) {
            throw new Exception("Invalid password. Must be at least 6 characters long and contain letters and digits");
        }

        // E-mail used as salt for now
        SaltedPasswordHash saltedPassword = new SaltedPasswordHash();
        saltedPassword.setPassword(password, user.getEmail());
        user.setPassword(saltedPassword);

        // Associate the user with the "user" role
        Role userRole = findRoleByName("user");
        user.getRoles().add(userRole);

        // Persist the user
        saveEntity(user);

        return user;
    }

    /**
     * First step of setting a new password.
     * A reset-password token is generated and an email sent to the user
     * with a link to reset the password.
     * @param email the email address
     */
    public void resetPassword(String email) throws Exception {
        User user = findByEmail(email);
        if (user == null) {
            throw new Exception("Invalid email " + email);
        }

        user.setResetPasswordToken(UUID.randomUUID().toString());
        saveEntity(user);

        Map<String, Object> data = new HashMap<>();
        data.put("token", user.getResetPasswordToken());
        data.put("name", user.getName());
        data.put("email", user.getEmail());

        mailService.sendMail("reset-password.ftl", data, "Reset Password", email);
    }

    /**
     * Second step of setting a new password.
     * The used submits the token generated by calling {@code resetPassword()}
     * along with the new password.
     *
     * @param email the email address
     *
     */
    public void updatePassword(String email, String password, String token) throws Exception {
        User user = findByEmail(email);
        if (user == null) {
            throw new Exception("Invalid email " + email);
        }

        if (!token.equals(user.getResetPasswordToken())) {
            throw new Exception("Invalid token " + token);
        }

        // Validate the password strength
        if (!validatePasswordStrength(password)) {
            throw new Exception("Invalid password. Must be at least 6 characters long and contain letters and digits");
        }

        // E-mail used as salt for now
        user.getPassword().setPassword(password, email);

        // Reset the password token, so the same mail cannot be used again...
        user.setResetPasswordToken(null);

        // Persist the user entity
        saveEntity(user);

        // Flush the jboss JAAS cache
        jbossJaasCacheFlusher.flushJaasCache(email);
    }
}
