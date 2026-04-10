package com.b4rrhh.shared.infrastructure.dev.auth;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DevAuthTokenController.class)
@ActiveProfiles("local")
@Import({DevAuthTokenController.class, DevAuthTokenService.class, com.b4rrhh.shared.infrastructure.config.SecurityConfig.class})
@TestPropertySource(properties = {
        "app.jwt.secret=b4rrhh-dev-secret-key-minimum-256-bits-for-hmac-sha256-algorithm",
        "app.dev-auth.enabled=false"
})
class DevAuthTokenControllerDisabledHttpTest {

    private static final String SECRET = "b4rrhh-dev-secret-key-minimum-256-bits-for-hmac-sha256-algorithm";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void endpointIsUnavailableWhenDevAuthIsDisabled() throws Exception {
        mockMvc.perform(post("/dev/auth/token")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                                                    "subject": "bifor"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    private String jwtToken() throws JOSEException {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("bifor")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(3600)))
                .build();

        SignedJWT signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        signedJwt.sign(new MACSigner(SECRET.getBytes(StandardCharsets.UTF_8)));
        return signedJwt.serialize();
    }
}