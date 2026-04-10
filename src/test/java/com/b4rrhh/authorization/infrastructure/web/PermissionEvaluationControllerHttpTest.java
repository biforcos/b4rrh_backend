package com.b4rrhh.authorization.infrastructure.web;

import com.b4rrhh.authorization.application.usecase.EvaluatePermissionCommand;
import com.b4rrhh.authorization.application.usecase.EvaluatePermissionUseCase;
import com.b4rrhh.authorization.application.usecase.ResolveSubjectRolesUseCase;
import com.b4rrhh.authorization.domain.model.PermissionDecision;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
        @Mock
        private ResolveSubjectRolesUseCase resolveSubjectRolesUseCase;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        PermissionEvaluationBusinessKeyController controller = new PermissionEvaluationBusinessKeyController(
                evaluatePermissionUseCase,
                resolveSubjectRolesUseCase
        );
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new PermissionEvaluationExceptionHandler())
                .build();
    }

    @Test
    void returnsAllowWhenPermissionGranted() throws Exception {
        when(resolveSubjectRolesUseCase.resolveActiveRoleCodes("user1"))
                .thenReturn(List.of("HR_MANAGER"));
        when(evaluatePermissionUseCase.evaluate(any()))
                .thenReturn(PermissionDecision.allow("Granted by role HR_MANAGER"));

        mockMvc.perform(post("/authorization/evaluate")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"resourceCode": "EMPLOYEE.CONTACT", "actionCode": "READ"}
                                """)
                        .principal(jwtToken("user1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("ALLOW"))
                .andExpect(jsonPath("$.reason").value("Granted by role HR_MANAGER"));
    }

    @Test
    void returnsDenyWhenPermissionNotGranted() throws Exception {
        when(resolveSubjectRolesUseCase.resolveActiveRoleCodes("user1"))
                .thenReturn(List.of("HR_OPERATOR"));
        when(evaluatePermissionUseCase.evaluate(any()))
                .thenReturn(PermissionDecision.deny("No role grants action"));

        mockMvc.perform(post("/authorization/evaluate")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"resourceCode": "EMPLOYEE.LIFECYCLE.TERMINATE", "actionCode": "EXECUTE"}
                                """)
                        .principal(jwtToken("user1")))
                .andExpect(status().isOk())
                        .andExpect(jsonPath("$.decision").value("DENY"))
                        .andExpect(jsonPath("$.reason").value("No role grants action"));
    }

    @Test
        void resolvesRolesFromSubjectAndPassesThemToCommand() throws Exception {
                when(resolveSubjectRolesUseCase.resolveActiveRoleCodes("jdoe"))
                                .thenReturn(List.of("HR_MANAGER", "AUDITOR"));
        when(evaluatePermissionUseCase.evaluate(any()))
                .thenReturn(PermissionDecision.allow("ok"));

        mockMvc.perform(post("/authorization/evaluate")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"resourceCode": "EMPLOYEE.CONTACT", "actionCode": "READ"}
                                """)
                        .principal(jwtToken("jdoe")))
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
        when(resolveSubjectRolesUseCase.resolveActiveRoleCodes("user1"))
                .thenReturn(List.of("HR_MANAGER"));
        when(evaluatePermissionUseCase.evaluate(any()))
                .thenReturn(PermissionDecision.deny("Denied because secured resource 'UNKNOWN' was not found"));

        mockMvc.perform(post("/authorization/evaluate")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"resourceCode": "UNKNOWN", "actionCode": "READ"}
                                """)
                        .principal(jwtToken("user1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("DENY"))
                .andExpect(jsonPath("$.reason").value("Denied because secured resource 'UNKNOWN' was not found"));
    }

    @Test
    void returns400WhenResourceCodeIsBlank() throws Exception {
        when(resolveSubjectRolesUseCase.resolveActiveRoleCodes("user1"))
                .thenReturn(List.of("HR_MANAGER"));
        when(evaluatePermissionUseCase.evaluate(any()))
                .thenThrow(new IllegalArgumentException("resourceCode is required"));

        mockMvc.perform(post("/authorization/evaluate")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"resourceCode": "", "actionCode": "READ"}
                                """)
                        .principal(jwtToken("user1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("resourceCode is required"));
    }

    @Test
    void passesEmptyRoleListWhenSubjectHasNoAssignments() throws Exception {
        when(resolveSubjectRolesUseCase.resolveActiveRoleCodes("user1"))
                .thenReturn(List.of());
        when(evaluatePermissionUseCase.evaluate(any()))
                .thenReturn(PermissionDecision.deny("Denied because subject 'user1' has no roles to evaluate action 'READ' on resource 'EMPLOYEE.CONTACT'"));

        mockMvc.perform(post("/authorization/evaluate")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"resourceCode": "EMPLOYEE.CONTACT", "actionCode": "READ"}
                                """)
                        .principal(jwtToken("user1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("DENY"));

        ArgumentCaptor<EvaluatePermissionCommand> captor = ArgumentCaptor.forClass(EvaluatePermissionCommand.class);
        verify(evaluatePermissionUseCase).evaluate(captor.capture());

        assertThat(captor.getValue().roleCodes()).isEmpty();
    }

    @Test
    void preservesAuthenticatedSubjectCaseWhenResolvingInternalRoles() throws Exception {
        when(resolveSubjectRolesUseCase.resolveActiveRoleCodes("BiFor"))
                .thenReturn(List.of("ADMIN"));
        when(evaluatePermissionUseCase.evaluate(any()))
                .thenReturn(PermissionDecision.allow("ok"));

        mockMvc.perform(post("/authorization/evaluate")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"resourceCode": "EMPLOYEE.CONTACT", "actionCode": "READ"}
                                """)
                        .principal(jwtToken("BiFor")))
                .andExpect(status().isOk());

        verify(resolveSubjectRolesUseCase).resolveActiveRoleCodes("BiFor");

        ArgumentCaptor<EvaluatePermissionCommand> captor = ArgumentCaptor.forClass(EvaluatePermissionCommand.class);
        verify(evaluatePermissionUseCase).evaluate(captor.capture());

        assertThat(captor.getValue().subject()).isEqualTo("BiFor");
        assertThat(captor.getValue().roleCodes()).containsExactly("ADMIN");
    }

    private JwtAuthenticationToken jwtToken(String subject) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject(subject)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        return new JwtAuthenticationToken(jwt);
    }
}
