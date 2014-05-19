package dk.dma.msinm.user.security.shiro;

import dk.dma.msinm.common.config.CdiHelper;
import dk.dma.msinm.user.User;
import dk.dma.msinm.user.UserService;
import dk.dma.msinm.user.security.JWTService;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;

/**
 * An implementation of the Shiro realm
 */
public class UserRealm extends AuthorizingRealm {

    public static final String REALM = "MsiNmUserRealm";

    /**
     * Constructor
     */
    public UserRealm() {
        setName(REALM);
        HashedCredentialsMatcher credentialsMatcher = new HashedCredentialsMatcher();
        credentialsMatcher.setHashAlgorithmName("SHA-256");
        setCredentialsMatcher(credentialsMatcher);

        System.out.println("XXXXXX INIT REALM");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) {

        System.out.println("XXXXXX AUTH INFO");

        UsernamePasswordToken token = (UsernamePasswordToken) authcToken;

        // Null username is invalid
        if (token.getUsername() == null || token.getUsername().trim().length() == 0) {
            throw new AccountException("Empty usernames are not allowed");
        }

        User user = null;
        try {
            user = CdiHelper.getBean(UserService.class).findByEmail(token.getUsername());
        } catch (Exception ex) {
            throw new AuthenticationException(ex);
        }

        if (user == null) {
            throw new AccountException("No user with id " + token.getUsername());
        } else if (!user.hasPassword()) {
            throw new AccountException("User " + token.getUsername() + " has no valid password.");
        }

        return new SimpleAuthenticationInfo(
                user.getId(),
                user.getPassword().getPasswordHash().toCharArray(),
                ByteSource.Util.bytes(user.getPassword().getPasswordSalt()),
                getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void assertCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) throws AuthenticationException {

        // Bearer token authentication
        if (token instanceof BearerToken) {
            BearerToken bearerToken = (BearerToken)token;
            try {
                JWTService jwtService = CdiHelper.getBean(JWTService.class);
                if (!jwtService.checkValidBearerToken(bearerToken.getToken())) {
                    throw new AuthenticationException("Invalid bearer token");
                }
            } catch (Exception ex) {
                throw new AuthenticationException("Invalid bearer token");
            }


        } else {
            // Username - password authentication
            super.assertCredentialsMatch(token, info);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

        //null usernames are invalid
        if (principals == null) {
            throw new AuthorizationException("PrincipalCollection method argument cannot be null");
        }

        // Check that the user was authenticated with this realm
        if (principals.fromRealm(getName()).isEmpty())  {
            return null;
        }

        try {
            UserService userService = CdiHelper.getBean(UserService.class);
            Long userId = (Long) principals.fromRealm(getName()).iterator().next();
            User user = userService.getByPrimaryKey(User.class, userId);
            if (user != null) {
                SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
                info.addRole("user");
                return info;
            } else {
                return null;
            }
        } catch (Exception ex) {
            throw new AuthorizationException("Error looking up user");
        }
    }
}

