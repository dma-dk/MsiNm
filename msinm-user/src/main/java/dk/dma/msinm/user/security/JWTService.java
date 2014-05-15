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
import dk.dma.msinm.common.settings.annotation.Setting;
import dk.dma.msinm.user.User;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

/**
 * Service for handling Json Web Tokens (JWT).
 */
@Singleton
public class JWTService {


    @Inject
    Logger log;

    @Inject
    @Setting(value = "jwtTimeoutMinutes", defaultValue = "15")
    Long jwtTimeoutMinutes;

    @Inject
    @Setting(value = "jwtHmacSharedKey", defaultValue = "sdjkfhs-SALKJD-933409_JksdjfkA")
    String hmacSharedKey;

    /**
     * Creates a signed JWT for the given user
     * @param svr the server
     * @param user the user
     * @return the encrypted JWT
     */
    public String getSignedJWT(String svr, User user) throws Exception {
        long t0 = System.currentTimeMillis();


        // compose the JWT reserved claim names
        JWTClaimsSet jwtClaims = new JWTClaimsSet();
        jwtClaims.setIssuer(svr);
        jwtClaims.setSubject("" + user.getId());
        jwtClaims.setAudience(Arrays.asList(svr));
        jwtClaims.setExpirationTime(new Date(new Date().getTime() + 1000 * 60 * jwtTimeoutMinutes));
        jwtClaims.setNotBeforeTime(new Date());
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

        // Serialise to JWT compact form
        return jwsObject.serialize();
    }

    /**
     * Parses a JWT authorization header
     * @param token the JWT token
     * @return the parsed JWT
     */
    public String parseSignedJWT(String token) throws Exception {
        // Parse back and check signature
        JWSObject jwsObject = JWSObject.parse(token);

        JWSVerifier verifier = new MACVerifier(hmacSharedKey.getBytes());

        boolean verifiedSignature = jwsObject.verify(verifier);

        if (verifiedSignature)
            System.out.println("Verified JWS signature!");
        else
            System.out.println("Bad JWS signature!");

        Payload payload = jwsObject.getPayload();
        return payload.toString();
    }
}
