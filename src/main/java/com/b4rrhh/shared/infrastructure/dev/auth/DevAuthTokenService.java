package com.b4rrhh.shared.infrastructure.dev.auth;

import com.b4rrhh.shared.infrastructure.config.JwtProperties;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@Profile("local")
@ConditionalOnProperty(prefix = "app.dev-auth", name = "enabled", havingValue = "true")
public class DevAuthTokenService {

    private final JwtProperties jwtProperties;
    private final DevAuthProperties devAuthProperties;

    public DevAuthTokenService(JwtProperties jwtProperties, DevAuthProperties devAuthProperties) {
        this.jwtProperties = jwtProperties;
        this.devAuthProperties = devAuthProperties;
    }

    public DevAuthTokenResponse issueToken(DevAuthTokenRequest request) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(resolveExpiresInMinutes(request.expiresInMinutes()), ChronoUnit.MINUTES);
        String normalizedSubject = request.subject().trim();

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
            .subject(normalizedSubject)
                .issueTime(Date.from(issuedAt))
                .expirationTime(Date.from(expiresAt))
                .build();

        return new DevAuthTokenResponse(
                "Bearer",
                sign(claimsSet),
            normalizedSubject,
                expiresAt
        );
    }

    private int resolveExpiresInMinutes(Integer requestedExpiresInMinutes) {
        if (requestedExpiresInMinutes != null) {
            return requestedExpiresInMinutes;
        }
        return devAuthProperties.getDefaultExpiresInMinutes();
    }

    private String sign(JWTClaimsSet claimsSet) {
        SignedJWT signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        try {
            signedJwt.sign(new MACSigner(signingKeyBytes()));
        } catch (JOSEException e) {
            throw new IllegalStateException("Unable to sign dev JWT token", e);
        }
        return signedJwt.serialize();
    }

    private byte[] signingKeyBytes() {
        byte[] keyBytes = jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "app.jwt.secret must be at least 32 bytes (256 bits) for HMAC-SHA256. "
                            + "Current length: " + keyBytes.length + " bytes."
            );
        }
        return keyBytes;
    }
}