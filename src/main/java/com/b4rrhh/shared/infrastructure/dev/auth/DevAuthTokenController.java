package com.b4rrhh.shared.infrastructure.dev.auth;

import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("local")
@ConditionalOnProperty(prefix = "app.dev-auth", name = "enabled", havingValue = "true")
@RequestMapping("/dev/auth")
public class DevAuthTokenController {

    private final DevAuthTokenService devAuthTokenService;

    public DevAuthTokenController(DevAuthTokenService devAuthTokenService) {
        this.devAuthTokenService = devAuthTokenService;
    }

    @PostMapping("/token")
    public ResponseEntity<DevAuthTokenResponse> issueToken(@Valid @RequestBody DevAuthTokenRequest request) {
        return ResponseEntity.ok(devAuthTokenService.issueToken(request));
    }
}