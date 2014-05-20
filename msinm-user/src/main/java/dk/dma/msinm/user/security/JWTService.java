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
import dk.dma.msinm.user.Role;
import dk.dma.msinm.user.User;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for handling Json Web Tokens (JWT).
 */
@Singleton
public class JWTService {


    @Inject
    Logger log;

    @Inject
    @Setting(value = "jwtTimeoutMinutes", defaultValue = "1") // TODO: Set artificially low for test purposes
    Long jwtTimeoutMinutes;

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
        jwtClaims.setSubject("" + user.getId());
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
     * Parses a JWT authorization header. Returns null if the token cannot be parsed
     *
     * @param token the JWT token
     * @return the parsed JWT
     */
    public ParsedJWTInfo parseSignedJWT(String token) throws Exception {
        // Parse back and check signature
        SignedJWT signedJWT = SignedJWT.parse(token);

        JWSVerifier verifier = new MACVerifier(hmacSharedKey.getBytes());

        boolean verifiedSignature = signedJWT.verify(verifier);

        if (verifiedSignature) {
            ReadOnlyJWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            ParsedJWTInfo jwtInfo = new ParsedJWTInfo();
            jwtInfo.setSubject(claims.getSubject());
            jwtInfo.setExpirationTime(claims.getExpirationTime());
            return jwtInfo;
        }

        return null;
    }

    /**
     * Selected information from a parsed JWT token
     */
    public static class ParsedJWTInfo {
        String subject;
        Date expirationTime;

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
    }
}
