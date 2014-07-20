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
import dk.dma.msinm.common.templates.TemplateContext;
import dk.dma.msinm.common.templates.TemplateService;
import dk.dma.msinm.common.templates.TemplateType;
import dk.dma.msinm.user.security.JbossJaasCacheFlusher;
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
    private TemplateService templateService;

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
     * Looks up the {@code User} with the given email address and pre-loads the roles
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
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Called when a person registers a new user via the website.
     * The user will automatically get the "user" role.
     *
     * @param user the template user entity
     * @return the updated user
     */
    public User registerUser(User user) throws Exception {
        return registerUserWithRoles(user, true, "user");
    }

    /**
     * Called when a user has logged in via OAuth and did not exist in advance.
     * No activation email is sent.
     *
     * @param user the template user entity
     * @return the updated user
     */
    public User registerOAuthOnlyUser(User user) throws Exception {
        return registerUserWithRoles(user, false, "user");
    }

    /**
     * Registers a new user with the given roles.
     * Sends an activation email to the user.
     *
     * @param user the template user entity
     * @param sendEmail whether to send activation email or not
     * @param roles the list of roles to assign the user
     * @return the updated user
     */
    private User registerUserWithRoles(User user, boolean sendEmail, String... roles) throws Exception {
        // Validate that the email address is not already registered
        if (findByEmail(user.getEmail()) != null) {
            throw new Exception("Email " + user.getEmail() + " is already registered");
        }

        // Associate the user with the roles
        user.getRoles().clear();
        for (String role : roles) {
            user.getRoles().add(findRoleByName(role));
        }

        // Set a reset-password token
        if (sendEmail) {
            user.setResetPasswordToken(UUID.randomUUID().toString());
        }

        // Persist the user
        user = saveEntity(user);

        // Send registration email
        if (sendEmail) {
            Map<String, Object> data = new HashMap<>();
            data.put("token", user.getResetPasswordToken());
            data.put("name", user.getName());
            data.put("email", user.getEmail());

            sendEmail(data, "user-activation.ftl", "user.registration.subject", user);
        }

        return user;
    }

    /**
     * Called when an administrator creates or edits a user.
     *
     * @param user the template user entity
     * @param roles the list of roles to assign the user
     * @return the updated user
     */
    public User createOrUpdateUser(User user, String[] roles) throws Exception {
        // Check if the user is already registered
        User existnigUser = findByEmail(user.getEmail());

        if (existnigUser == null) {
            // Create a new user
            existnigUser = registerUserWithRoles(user, true, roles);

        } else {

            // Update the existing user
            existnigUser.setFirstName(user.getFirstName());
            existnigUser.setLastName(user.getLastName());
            existnigUser.getRoles().clear();
            for (String role : roles) {
                existnigUser.getRoles().add(findRoleByName(role));
            }

            existnigUser = saveEntity(existnigUser);
        }

        return existnigUser;
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

        // Send reset-password email
        Map<String, Object> data = new HashMap<>();
        data.put("token", user.getResetPasswordToken());
        data.put("name", user.getName());
        data.put("email", user.getEmail());
        sendEmail(data, "reset-password.ftl", "reset.password.subject", user);
    }

    /**
     * Sends an email based on the parameters
     * @param data the email data
     * @param template the Freemarker template
     * @param subjectKey the subject key
     * @param user the recipient user
     */
    private void sendEmail(Map<String, Object> data, String template, String subjectKey, User user) throws Exception {
        TemplateContext ctx = templateService.getTemplateContext(
                TemplateType.MAIL,
                template,
                data,
                user.getLanguage(),
                "Mails");

        String subject = ctx.getBundle().getString(subjectKey);
        String content = templateService.process(ctx);
        String baseUri = (String)ctx.getData().get("baseUri");
        mailService.sendMail(content, subject, baseUri, user.getEmail());
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

        if (user.getPassword() == null) {
            user.setPassword(new SaltedPasswordHash());
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
