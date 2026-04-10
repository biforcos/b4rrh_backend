package com.b4rrhh.shared.infrastructure.dev.auth;

import com.b4rrhh.shared.infrastructure.config.JwtProperties;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class DevAuthTokenServiceTest {

    private DevAuthTokenService service;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties("b4rrhh-dev-secret-key-minimum-256-bits-for-hmac-sha256-algorithm");
        DevAuthProperties devAuthProperties = new DevAuthProperties();
        devAuthProperties.setEnabled(true);
        devAuthProperties.setDefaultExpiresInMinutes(60);
        service = new DevAuthTokenService(jwtProperties, devAuthProperties);
    }

    @Test
    void generatesSignedTokenWithExpectedSubjectWithoutRolesClaim() throws ParseException {
        DevAuthTokenResponse response = service.issueToken(new DevAuthTokenRequest(
                "bifor",
                120
        ));

        SignedJWT signedJwt = SignedJWT.parse(response.token());

        assertEquals("Bearer", response.tokenType());
        assertEquals("bifor", response.subject());
        assertEquals("bifor", signedJwt.getJWTClaimsSet().getSubject());
        assertNull(signedJwt.getJWTClaimsSet().getClaim("roles"));
        assertNotNull(signedJwt.getJWTClaimsSet().getExpirationTime());
    }

    @Test
    void usesDefaultExpirationWhenRequestOmitsIt() {
        DevAuthTokenResponse response = service.issueToken(new DevAuthTokenRequest(
                "bifor",
                null
        ));

        assertNotNull(response.expiresAt());
    }

    @Test
    void trimsSubjectWithoutChangingCase() throws ParseException {
        DevAuthTokenResponse response = service.issueToken(new DevAuthTokenRequest(
                " BiFor ",
                120
        ));

        SignedJWT signedJwt = SignedJWT.parse(response.token());

        assertEquals("BiFor", response.subject());
        assertEquals("BiFor", signedJwt.getJWTClaimsSet().getSubject());
        assertNull(signedJwt.getJWTClaimsSet().getClaim("roles"));
    }
}