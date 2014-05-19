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
package dk.dma.msinm.user.sercurity;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Test signing of JWT tokens
 */
public class SignedJWTTest {

    private static final String SHARED_KEY = "MY MSINM Token";

    @Test
    public void testSignedJWT() throws JOSEException, ParseException {

        JWTClaimsSet jwtClaims = new JWTClaimsSet();
        jwtClaims.setIssuer("https://my-auth-server.com");
        jwtClaims.setSubject("Mariusz");
        List<String> aud = new ArrayList<>();
        aud.add("https://my-web-app.com");
        aud.add("https://your-web-app.com");
        jwtClaims.setAudience(aud);
        jwtClaims.setExpirationTime(new Date(new Date().getTime() + 1000 * 60 * 10));
        jwtClaims.setNotBeforeTime(new Date());
        jwtClaims.setIssueTime(new Date());
        jwtClaims.setJWTID(UUID.randomUUID().toString());

        // Create JWS header with HS256 algorithm
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
        header.setContentType("text/plain");

        // Create JWS object
        JWSObject jwsObject = new JWSObject(header, new Payload(jwtClaims.toJSONObject()));

        // Create HMAC signer
        JWSSigner signer = new MACSigner(SHARED_KEY.getBytes());

        // Sign the JWT
        jwsObject.sign(signer);

        // Serialise to JWT compact form
        String result = jwsObject.serialize();
        System.out.println(result);

        // Parse back and check signature
        SignedJWT signedJWT = SignedJWT.parse(result);

        JWSVerifier verifier = new MACVerifier(SHARED_KEY.getBytes());

        boolean verifiedSignature = signedJWT.verify(verifier);

        if (verifiedSignature)
            System.out.println("Verified JWS signature!");
        else
            System.out.println("Bad JWS signature!");

        System.out.println("Recovered payload message: " + jwsObject.getPayload());

    }
}
