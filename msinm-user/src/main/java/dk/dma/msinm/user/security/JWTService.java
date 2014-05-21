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

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import dk.dma.msinm.common.settings.annotation.Setting;
import dk.dma.msinm.user.User;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.security.auth.login.CredentialExpiredException;
import java.util.*;

/**
 * Service for handling Json Web Tokens (JWT).
 */
@Singleton
public class JWTService {

    private static ThreadLocal<String> THREAD_TEMP_JWT_PWD_TOKEN = new ThreadLocal<>();

    @Inject
    Logger log;

    @Inject
    @Setting(value = "jwtTimeoutMinutes", defaultValue = "30")
    Long jwtTimeoutMinutes;

    @Inject
    @Setting(value = "jwtReauthMinutes", defaultValue = "10") 
    Long jwtReauthMinutes;

    @Inject
    @Setting(value = "jwtHmacSharedKey", defaultValue = "sdjkfhs-SALKJD-933409_JksdjfkA")
    String hmacSharedKey;

    /**
     * Creates a signed JWT for the given user
     * @param issuer the issuer
     * @param user the user
     * @return the encrypted JWT
     */
    public JWTToken createSignedJWT(String issuer, User user) throws Exception {

        // compose the JWT reserved claim names
        JWTClaimsSet jwtClaims = new JWTClaimsSet();
        jwtClaims.setSubject(user.getEmail());
        jwtClaims.setIssuer(issuer);
        jwtClaims.setAudience(Arrays.asList(issuer));
        jwtClaims.setExpirationTime(new Date(new Date().getTime() + 1000 * 60 * jwtTimeoutMinutes));
        jwtClaims.setIssueTime(new Date());
        jwtClaims.setJWTID(UUID.randomUUID().toString());

        // Create JWS header with HS256 algorithm
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
        header.setContentType("text/plain");

        // Create JWS object
        JWSObject jwsObject = new JWSObject(header, new Payload(jwtClaims.toJSONObject()));

        // Create HMAC signer
        JWSSigner signer = new MACSigner(hmacSharedKey.getBytes());

        // Sign the JWT
        jwsObject.sign(signer);

        // Fill out the resulting JWT token
        JWTToken token = new JWTToken();
        token.setToken(jwsObject.serialize());
        token.setEmail(user.getEmail());
        token.setName(user.getName());
        List<String> roles = new ArrayList<>();
        user.getRoles().forEach(r -> roles.add(r.getName()));
        token.setRoles(roles.toArray(new String[roles.size()]));
        return token;
    }

    /**
     * Parses a JWT authorization header. Throws an exception if the JWT cannot be verified
     *
     * @param token the JWT token
     * @return the parsed JWT
     */
    public ParsedJWTInfo parseSignedJWT(String token) throws Exception {
        // Parse back and check signature
        SignedJWT signedJWT = SignedJWT.parse(token);

        JWSVerifier verifier = new MACVerifier(hmacSharedKey.getBytes());

        boolean verifiedSignature = signedJWT.verify(verifier);

        if (!verifiedSignature) {
            throw new CredentialExpiredException("JWT token expired");
        }

        ReadOnlyJWTClaimsSet claims = signedJWT.getJWTClaimsSet();

        // Collect the interesting information in a ParsedJWTInfo and return it
        ParsedJWTInfo jwtInfo = new ParsedJWTInfo();
        jwtInfo.setSubject(claims.getSubject());
        jwtInfo.setExpirationTime(claims.getExpirationTime());
        jwtInfo.setIssueTime(claims.getIssueTime());
        return jwtInfo;
    }

    /**
     * Checks to see if it is time to issue a new JWT token
     * @param jwtInfo the current JWT token
     * @return if it is time to issue a new JWT token
     */
    public boolean reauthJWT(ParsedJWTInfo jwtInfo) {
        double minutesOld = (new Date().getTime() - jwtInfo.getIssueTime().getTime()) / 1000.0 / 60.0;
        return minutesOld > jwtReauthMinutes.doubleValue();
    }

    /**
     * Generates a temporary password token. This is generated in the SecurityServletFilter
     * and used in the JbossLoginModule in the same synchronous request.
     * @param prefix a prefix
     * @return the temporary password token
     */
    public String generateTempJwtPwdToken(String prefix) {
        String pwd = StringUtils.defaultString(prefix) + UUID.randomUUID().toString();
        THREAD_TEMP_JWT_PWD_TOKEN.set(pwd);
        return pwd;
    }

    /**
     * Verifies that the given password is the one associated with the current thread.
     * At the same time, it removes the password from the current thread, so, it can
     * only be called onece.
     * @param pwd the password to check
     * @return if the password matches the one of the current thread
     */
    public boolean verifyTempJwtPwdToken(String pwd) {
        String threadPwd = THREAD_TEMP_JWT_PWD_TOKEN.get();
        THREAD_TEMP_JWT_PWD_TOKEN.remove();
        return pwd.equals(threadPwd);
    }

    /**
     * Selected information from a parsed JWT token
     */
    public static class ParsedJWTInfo {
        String subject;
        Date expirationTime;
        Date issueTime;

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public Date getExpirationTime() {
            return expirationTime;
        }

        public void setExpirationTime(Date expirationTime) {
            this.expirationTime = expirationTime;
        }

        public Date getIssueTime() {
            return issueTime;
        }

        public void setIssueTime(Date issueTime) {
            this.issueTime = issueTime;
        }
    }
}
