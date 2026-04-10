package com.b4rrhh.shared.infrastructure.dev.auth;

import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DevAuthTokenController.class)
@ActiveProfiles("local")
@Import({DevAuthTokenController.class, DevAuthTokenService.class, com.b4rrhh.shared.infrastructure.config.SecurityConfig.class})
@TestPropertySource(properties = {
        "app.jwt.secret=b4rrhh-dev-secret-key-minimum-256-bits-for-hmac-sha256-algorithm",
        "app.dev-auth.enabled=true",
        "app.dev-auth.default-expires-in-minutes=60"
})
class DevAuthTokenControllerEnabledHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsSignedTokenForValidRequest() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/dev/auth/token")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "subject": "bifor",
                                  "expiresInMinutes": 120
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.subject").value("bifor"))
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        String token = responseBody.replaceAll("(?s).*\"token\":\"([^\"]+)\".*", "$1");

        SignedJWT signedJwt = SignedJWT.parse(token);
        assertEquals("bifor", signedJwt.getJWTClaimsSet().getSubject());
    assertNull(signedJwt.getJWTClaimsSet().getClaim("roles"));
    }

    @Test
    void rejectsInvalidRequest() throws Exception {
        mockMvc.perform(post("/dev/auth/token")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "subject": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}