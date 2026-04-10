package com.b4rrhh.shared.infrastructure.dev.auth;

import java.time.Instant;

public record DevAuthTokenResponse(
        String tokenType,
        String token,
        String subject,
        Instant expiresAt
) {
}