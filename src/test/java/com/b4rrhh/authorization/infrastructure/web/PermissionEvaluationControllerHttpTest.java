package com.b4rrhh.authorization.infrastructure.web;

import com.b4rrhh.authorization.application.usecase.EvaluatePermissionCommand;
import com.b4rrhh.authorization.application.usecase.EvaluatePermissionUseCase;
import com.b4rrhh.authorization.domain.model.PermissionDecision;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PermissionEvaluationBusinessKeyControllerHttpTest {

    @Mock
    private EvaluatePermissionUseCase evaluatePermissionUseCase;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        PermissionEvaluationBusinessKeyController controller = new PermissionEvaluationBusinessKeyController(evaluatePermissionUseCase);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new PermissionEvaluationExceptionHandler())
                .build();
    }

    @Test
    void returnsAllowWhenPermissionGranted() throws Exception {
        when(evaluatePermissionUseCase.evaluate(any()))
                .thenReturn(PermissionDecision.allow("Granted by role HR_MANAGER"));

        mockMvc.perform(post("/authorization/evaluate")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"resourceCode": "EMPLOYEE.CONTACT", "actionCode": "READ"}
                                """)
                        .principal(jwtToken("user1", List.of("HR_MANAGER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("ALLOW"))
                .andExpect(jsonPath("$.reason").value("Granted by role HR_MANAGER"));
    }

    @Test
    void returnsDenyWhenPermissionNotGranted() throws Exception {
        when(evaluatePermissionUseCase.evaluate(any()))
                .thenReturn(PermissionDecision.deny("No role grants action"));

        mockMvc.perform(post("/authorization/evaluate")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"resourceCode": "EMPLOYEE.LIFECYCLE.TERMINATE", "actionCode": "EXECUTE"}
                                """)
                        .principal(jwtToken("user1", List.of("HR_OPERATOR"))))
                .andExpect(status().isOk())
                        .andExpect(jsonPath("$.decision").value("DENY"))
                        .andExpect(jsonPath("$.reason").value("No role grants action"));
    }

    @Test
    void passesSubjectAndRolesFromAuthenticationToCommand() throws Exception {
        when(evaluatePermissionUseCase.evaluate(any()))
                .thenReturn(PermissionDecision.allow("ok"));

        mockMvc.perform(post("/authorization/evaluate")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"resourceCode": "EMPLOYEE.CONTACT", "actionCode": "READ"}
                                """)
                        .principal(jwtToken("jdoe", List.of("HR_MANAGER", "AUDITOR"))))
                .andExpect(status().isOk());

        ArgumentCaptor<EvaluatePermissionCommand> captor = ArgumentCaptor.forClass(EvaluatePermissionCommand.class);
        verify(evaluatePermissionUseCase).evaluate(captor.capture());

        EvaluatePermissionCommand command = captor.getValue();
        assertThat(command.subject()).isEqualTo("jdoe");
        assertThat(command.roleCodes()).containsExactlyInAnyOrder("HR_MANAGER", "AUDITOR");
        assertThat(command.resourceCode()).isEqualTo("EMPLOYEE.CONTACT");
        assertThat(command.actionCode()).isEqualTo("READ");
    }

    @Test
    void returnsDenyWhenResourceNotFound() throws Exception {
        when(evaluatePermissionUseCase.evaluate(any()))
                .thenReturn(PermissionDecision.deny("Denied because secured resource 'UNKNOWN' was not found"));

        mockMvc.perform(post("/authorization/evaluate")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"resourceCode": "UNKNOWN", "actionCode": "READ"}
                                """)
                        .principal(jwtToken("user1", List.of("HR_MANAGER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("DENY"))
                .andExpect(jsonPath("$.reason").value("Denied because secured resource 'UNKNOWN' was not found"));
    }

    @Test
    void returns400WhenResourceCodeIsBlank() throws Exception {
        when(evaluatePermissionUseCase.evaluate(any()))
                .thenThrow(new IllegalArgumentException("resourceCode is required"));

        mockMvc.perform(post("/authorization/evaluate")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"resourceCode": "", "actionCode": "READ"}
                                """)
                        .principal(jwtToken("user1", List.of("HR_MANAGER"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("resourceCode is required"));
    }

    private JwtAuthenticationToken jwtToken(String subject, List<String> roles) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject(subject)
                .claim("roles", roles)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        return new JwtAuthenticationToken(jwt, authorities);
    }
}
