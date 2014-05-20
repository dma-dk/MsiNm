package dk.dma.msinm.user.security;

import dk.dma.msinm.common.config.CdiHelper;
import dk.dma.msinm.user.Role;
import dk.dma.msinm.user.User;
import dk.dma.msinm.user.UserService;
import org.jboss.security.SecurityConstants;
import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Custom login module that handles normal user/password authentication as well as JWT token
 * authentication.
 * <p>
 * The natural solution would be that this module set the {@code User} as the user principal
 * upon successful authentication.
 * <br>
 * However, this tends to cause ClassCastException's when the web-app has been reloaded,
 * because a different class-loader is used for the login-modules.
 * <br>
 * Hence, the login-module sets a {@code SimplePrincipal} as the request user principal, and
 * leave it up to the web-layer to swap the {@code SimplePrincipal} for a {@code User} principal.
 */
public class JbossLoginModule implements LoginModule {

    public static final String BEARER_TOKEN_LOGIN = "BEARER_TOKEN_LOGIN";

    private Logger log = LoggerFactory.getLogger(JbossLoginModule.class);

    protected Subject subject;
    protected CallbackHandler callbackHandler;
    protected Map sharedState;
    protected Map options;


    private User user;
    private Principal identity;

    private Throwable error;


    private UserService userService;
    private JWTService jwtService;

    /**
     * Constructor
     */
    public JbossLoginModule() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler,
                           Map<String,?> sharedState, Map<String,?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;
        this.error = null;

        try {
            this.userService = CdiHelper.getBean(UserService.class);
            this.jwtService = CdiHelper.getBean(JWTService.class);
        } catch (NamingException e) {
            log.error("Failed injecting UserService", e);
            error = e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean login() throws LoginException {

        if (error != null) {
            return false;
        }

        /**
        if (sharedState.get("javax.security.auth.login.name") != null &&
                sharedState.get("javax.security.auth.login.password") != null) {
            log.info("Using shared state user " + sharedState.get("javax.security.auth.login.name"));
            return true;
        }
         **/


        String[] credentials = getUserAndPassword();
        String emailOrJwt = credentials[0];
        String password = credentials[1];

        // If the password is BEARER_TOKEN_LOGIN, the user should be a JWT token
        if (BEARER_TOKEN_LOGIN.equals(password)) {
            try {
                JWTService.ParsedJWTInfo jwtInfo = jwtService.parseSignedJWT(emailOrJwt);
                if (jwtInfo != null) {

                    // Check if the bearer token has expired
                    Date now = new Date();
                    if (now.after(jwtInfo.getExpirationTime())) {
                        log.trace("JWT token for user %s expired at %s", jwtInfo.getSubject(), jwtInfo.getExpirationTime());
                        LoginException ex = new CredentialExpiredException("JWT token expired");
                        error = ex;
                        throw ex;
                    }

                    log.trace("Logging in using JWT token for user id " + jwtInfo.getSubject());
                    this.user = userService.findById(Integer.parseInt(jwtInfo.getSubject()));
                    this.identity = SecurityUtils.getPrincipal(this.user);
                }
            } catch (Exception ex) {
                log.trace("Error parsing JWT token");
            }

            if (this.user == null) {
                log.trace("Failed logging in with JWT token");
                LoginException ex = new LoginException("Failed logging in with JWT token");
                error = ex;
                throw ex;
            }



        } else {

            Exception cause = null;
            try {
                this.user = userService.findByEmail(emailOrJwt);
                this.identity = SecurityUtils.getPrincipal(user);
            } catch (Exception ex) {
                cause = ex;
            }
            if (this.user == null) {
                log.trace("Failed resolving user for " + emailOrJwt);
                LoginException ex = new LoginException("Failed resolving user for " + emailOrJwt);
                ex.initCause(cause);
                error = ex;
                throw ex;
            }

            log.trace("Resolved user " + this.user);

            // Compare the passwords
            if (!user.getPassword().equalsPassword(password)) {
                log.trace("Incorrect password for user " + emailOrJwt);
                FailedLoginException ex = new FailedLoginException("Incorrect password for user " + emailOrJwt);
                error = ex;
                throw ex;
            }

        }

        // Add the principal and password to the shared state map
        //sharedState.put("javax.security.auth.login.name", user);
        //sharedState.put("javax.security.auth.login.password", password);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean commit() throws LoginException {

        log.trace("Committing " + error);

        if (error != null) {
            return false;
        }

        subject.getPrincipals().add(identity);

        // Assign the roles to the subject
        Group group = new SimpleGroup("Roles");
        for (Role role : user.getRoles()) {
            group.addMember(new SimplePrincipal(role.getName()));
        }
        subject.getPrincipals().add(group);

        // add the CallerPrincipal group if none has been added in getRoleSets
        Group callerGroup = getCallerPrincipalGroup(subject.getPrincipals());
        if (callerGroup == null) {
            callerGroup = new SimpleGroup(SecurityConstants.CALLER_PRINCIPAL_GROUP);
            callerGroup.addMember(identity);
            subject.getPrincipals().add(callerGroup);
        }

        log.trace("Added groups");

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean abort() throws LoginException {
        log.trace("Aborting");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean logout() throws LoginException {
        log.trace("Logout");
        // Remove the user identity
        subject.getPrincipals().remove(identity);
        Group callerGroup = getCallerPrincipalGroup(subject.getPrincipals());
        if (callerGroup != null) {
            subject.getPrincipals().remove(callerGroup);
        }
        // Remove any added Groups...
        return true;
    }


    /**
     * Called by login() to acquire the user and password strings for
     * authentication. This method does no validation of either.
     * @return String[], [0] = username, [1] = password
     */
    protected String[] getUserAndPassword() throws LoginException
    {
        String[] info = {null, null};
        // prompt for a username and password
        if( callbackHandler == null ) {
            throw new FailedLoginException("No callback handler");
        }

        NameCallback nc = new NameCallback("Enter email address", "guest");
        PasswordCallback pc = new PasswordCallback("Enter password", false);
        Callback[] callbacks = {nc, pc};
        String username;
        String password = null;
        try  {
            callbackHandler.handle(callbacks);
            username = nc.getName();
            char[] tmpPassword = pc.getPassword();
            if( tmpPassword != null ) {
                char[] credential = new char[tmpPassword.length];
                System.arraycopy(tmpPassword, 0, credential, 0, tmpPassword.length);
                pc.clearPassword();
                password = new String(credential);
            }
        } catch(Exception e) {
            log.error("Failed to invoke callback");
            LoginException le = new LoginException("Failed to invoke callback");
            le.initCause(e);
            throw le;
        }
        info[0] = username;
        info[1] = password;
        return info;
    }


    protected Group getCallerPrincipalGroup(Set<Principal> principals)
    {
        Group callerGroup = null;
        for (Principal principal : principals)
        {
            if (principal instanceof Group)
            {
                Group group = Group.class.cast(principal);
                if (group.getName().equals(SecurityConstants.CALLER_PRINCIPAL_GROUP))
                {
                    callerGroup = group;
                    break;
                }
            }
        }
        return callerGroup;
    }
}

