package com.b4rrhh.shared.infrastructure.dev.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record DevAuthTokenRequest(
        @NotBlank String subject,
        @Positive Integer expiresInMinutes
) {
}