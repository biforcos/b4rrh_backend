package com.b4rrhh.shared.infrastructure.config;

import com.b4rrhh.authorization.application.usecase.EvaluatePermissionUseCase;
import com.b4rrhh.authorization.application.usecase.ResolveSubjectRolesUseCase;
import com.b4rrhh.authorization.domain.model.PermissionDecision;
import com.b4rrhh.authorization.infrastructure.web.PermissionEvaluationBusinessKeyController;
import com.b4rrhh.authorization.infrastructure.web.PermissionEvaluationExceptionHandler;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PermissionEvaluationBusinessKeyController.class)
@Import({SecurityConfig.class, PermissionEvaluationExceptionHandler.class})
@TestPropertySource(properties = "app.jwt.secret=b4rrhh-dev-secret-key-minimum-256-bits-for-hmac-sha256-algorithm")
class SecurityConfigTest {

    private static final String SECRET = "b4rrhh-dev-secret-key-minimum-256-bits-for-hmac-sha256-algorithm";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EvaluatePermissionUseCase evaluatePermissionUseCase;

    @MockBean
    private ResolveSubjectRolesUseCase resolveSubjectRolesUseCase;

    @Test
    void requiresAuthenticationByDefault() throws Exception {
        mockMvc.perform(get("/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void keepsActuatorHealthPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isNotFound());
    }

    @Test
    void allowsAuthenticatedAuthorizationEvaluation() throws Exception {
        when(resolveSubjectRolesUseCase.resolveActiveRoleCodes("user1")).thenReturn(java.util.List.of("HR_MANAGER"));
        when(evaluatePermissionUseCase.evaluate(any())).thenReturn(PermissionDecision.allow("ok"));

        mockMvc.perform(post("/authorization/evaluate")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"resourceCode": "EMPLOYEE.CONTACT", "actionCode": "READ"}
                                """)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken("user1")))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsAuthorizationEvaluationWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/authorization/evaluate")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"resourceCode": "EMPLOYEE.CONTACT", "actionCode": "READ"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void keepsDevAuthEndpointClosedOutsideLocalProfile() throws Exception {
        mockMvc.perform(post("/dev/auth/token")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"subject": "bifor"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    private String jwtToken(String subject) throws JOSEException {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(subject)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(3600)))
                .build();

        SignedJWT signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        signedJwt.sign(new MACSigner(SECRET.getBytes(StandardCharsets.UTF_8)));
        return signedJwt.serialize();
    }
}