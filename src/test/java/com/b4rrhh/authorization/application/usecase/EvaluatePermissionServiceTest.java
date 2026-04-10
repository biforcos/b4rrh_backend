package com.b4rrhh.authorization.application.usecase;

import com.b4rrhh.authorization.domain.model.PermissionDecision;
import com.b4rrhh.authorization.domain.model.PermissionProfile;
import com.b4rrhh.authorization.domain.model.PolicyEffect;
import com.b4rrhh.authorization.domain.model.PropagationMode;
import com.b4rrhh.authorization.domain.model.RoleResourcePolicy;
import com.b4rrhh.authorization.domain.model.SecuredResource;
import com.b4rrhh.authorization.domain.model.SecuredResourceKind;
import com.b4rrhh.authorization.domain.port.PermissionProfileRepository;
import com.b4rrhh.authorization.domain.port.RoleResourcePolicyRepository;
import com.b4rrhh.authorization.domain.port.SecuredResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluatePermissionServiceTest {

    @Mock
    private SecuredResourceRepository securedResourceRepository;
    @Mock
    private RoleResourcePolicyRepository policyRepository;
    @Mock
    private PermissionProfileRepository profileRepository;

    private EvaluatePermissionService service;

    @BeforeEach
    void setUp() {
        service = new EvaluatePermissionService(securedResourceRepository, policyRepository, profileRepository);
    }

    @Test
    void allowsWhenExactPolicyGrantsAction() {
        stubResource("EMPLOYEE.CONTACT", "EMPLOYEE");
        stubResource("EMPLOYEE", null);
        stubPolicy("HR_MANAGER", "EMPLOYEE.CONTACT", "SLOT_MAINTAINER", PolicyEffect.ALLOW, PropagationMode.THIS_RESOURCE_ONLY, true);
        stubProfile("SLOT_MAINTAINER", Set.of("READ", "CREATE", "UPDATE", "DELETE"));

        PermissionDecision decision = service.evaluate(command(List.of("HR_MANAGER"), "EMPLOYEE.CONTACT", "DELETE"));

        assertTrue(decision.isAllowed());
        assertTrue(decision.reason().contains("HR_MANAGER"));
        assertTrue(decision.reason().contains("SLOT_MAINTAINER"));
        assertTrue(decision.reason().contains("exact resource policy"));
    }

    @Test
    void allowsWhenInheritedPolicyWithPropagationGrantsAction() {
        stubResource("EMPLOYEE.LIFECYCLE.TERMINATE", "EMPLOYEE.LIFECYCLE");
        stubResource("EMPLOYEE.LIFECYCLE", "EMPLOYEE");
        stubResource("EMPLOYEE", null);
        stubPolicy("HR_MANAGER", "EMPLOYEE.LIFECYCLE", "WORKFLOW_EXECUTOR", PolicyEffect.ALLOW, PropagationMode.THIS_RESOURCE_AND_CHILDREN, true);
        stubProfile("WORKFLOW_EXECUTOR", Set.of("READ", "EXECUTE"));
        when(policyRepository.findByRoleCodeAndResourceCode("HR_MANAGER", "EMPLOYEE.LIFECYCLE.TERMINATE"))
                .thenReturn(Optional.empty());

        PermissionDecision decision = service.evaluate(command(List.of("HR_MANAGER"), "EMPLOYEE.LIFECYCLE.TERMINATE", "EXECUTE"));

        assertTrue(decision.isAllowed());
        assertTrue(decision.reason().contains("EMPLOYEE.LIFECYCLE"));
        assertTrue(decision.reason().contains("inherited ancestor policy"));
    }

        @Test
        void allowsWhenOneRoleGrantsAndNoDenyExists() {
        stubResource("EMPLOYEE.CONTACT", "EMPLOYEE");
        stubResource("EMPLOYEE", null);
        when(policyRepository.findByRoleCodeAndResourceCode("HR_VIEWER", "EMPLOYEE.CONTACT"))
            .thenReturn(Optional.empty());
        when(policyRepository.findByRoleCodeAndResourceCode("HR_VIEWER", "EMPLOYEE"))
            .thenReturn(Optional.empty());
        when(policyRepository.findByRoleCodeAndResourceCode("AUDITOR", "EMPLOYEE.CONTACT"))
            .thenReturn(Optional.empty());
        stubPolicy("AUDITOR", "EMPLOYEE", "READ_ONLY", PolicyEffect.ALLOW, PropagationMode.THIS_RESOURCE_AND_CHILDREN, true);
        stubProfile("READ_ONLY", Set.of("READ"));

        PermissionDecision decision = service.evaluate(command(List.of("HR_VIEWER", "AUDITOR"), "EMPLOYEE.CONTACT", "READ"));

        assertTrue(decision.isAllowed());
        assertTrue(decision.reason().contains("AUDITOR"));
        }

    @Test
    void deniesWhenAncestorPolicyHasThisResourceOnlyPropagation() {
        stubResource("EMPLOYEE.CONTACT", "EMPLOYEE");
        stubResource("EMPLOYEE", null);
        when(policyRepository.findByRoleCodeAndResourceCode("HR_VIEWER", "EMPLOYEE.CONTACT"))
                .thenReturn(Optional.empty());
        stubPolicy("HR_VIEWER", "EMPLOYEE", "READ_ONLY", PolicyEffect.ALLOW, PropagationMode.THIS_RESOURCE_ONLY, true);

        PermissionDecision decision = service.evaluate(command(List.of("HR_VIEWER"), "EMPLOYEE.CONTACT", "READ"));

        assertFalse(decision.isAllowed());
        assertTrue(decision.reason().contains("no applicable policy"));
    }

    @Test
    void ignoresInactivePolicy() {
        stubResource("EMPLOYEE.CONTACT", "EMPLOYEE");
        stubResource("EMPLOYEE", null);
        stubPolicy("HR_MANAGER", "EMPLOYEE.CONTACT", "SLOT_MAINTAINER", PolicyEffect.ALLOW, PropagationMode.THIS_RESOURCE_ONLY, false);

        PermissionDecision decision = service.evaluate(command(List.of("HR_MANAGER"), "EMPLOYEE.CONTACT", "READ"));

        assertFalse(decision.isAllowed());
        assertTrue(decision.reason().contains("no applicable policy"));
    }

    @Test
    void deniesWhenNoApplicablePolicyExists() {
        stubResource("EMPLOYEE.CONTACT", "EMPLOYEE");
        stubResource("EMPLOYEE", null);
        when(policyRepository.findByRoleCodeAndResourceCode("CATALOG_MANAGER", "EMPLOYEE.CONTACT"))
                .thenReturn(Optional.empty());
        when(policyRepository.findByRoleCodeAndResourceCode("CATALOG_MANAGER", "EMPLOYEE"))
                .thenReturn(Optional.empty());

        PermissionDecision decision = service.evaluate(command(List.of("CATALOG_MANAGER"), "EMPLOYEE.CONTACT", "READ"));

        assertFalse(decision.isAllowed());
        assertTrue(decision.reason().contains("no applicable policy"));
        assertTrue(decision.reason().contains("CATALOG_MANAGER"));
    }

    @Test
    void deniesExplicitlyWhenPermissionProfileIsMissing() {
        stubResource("EMPLOYEE.CONTACT", "EMPLOYEE");
        stubResource("EMPLOYEE", null);
        stubPolicy("HR_MANAGER", "EMPLOYEE.CONTACT", "MISSING_PROFILE", PolicyEffect.ALLOW, PropagationMode.THIS_RESOURCE_ONLY, true);
        when(profileRepository.findByCode("MISSING_PROFILE")).thenReturn(Optional.empty());

        PermissionDecision decision = service.evaluate(command(List.of("HR_MANAGER"), "EMPLOYEE.CONTACT", "READ"));

        assertFalse(decision.isAllowed());
        assertTrue(decision.reason().contains("permission profile 'MISSING_PROFILE'"));
        assertTrue(decision.reason().contains("was not found"));
    }

    @Test
    void deniesWhenActionNotGrantedByProfile() {
        stubResource("EMPLOYEE.LIFECYCLE.TERMINATE", "EMPLOYEE.LIFECYCLE");
        stubResource("EMPLOYEE.LIFECYCLE", "EMPLOYEE");
        stubResource("EMPLOYEE", null);
        stubPolicy("HR_OPERATOR", "EMPLOYEE.LIFECYCLE.TERMINATE", "NONE", PolicyEffect.ALLOW, PropagationMode.THIS_RESOURCE_ONLY, true);
        stubProfile("NONE", Set.of());

        PermissionDecision decision = service.evaluate(command(List.of("HR_OPERATOR"), "EMPLOYEE.LIFECYCLE.TERMINATE", "EXECUTE"));

        assertFalse(decision.isAllowed());
        assertTrue(decision.reason().contains("action 'EXECUTE' is not granted"));
    }

    @Test
    void explicitDenyOnExactResourceOverridesInheritedAllow() {
        stubResource("EMPLOYEE.LIFECYCLE.TERMINATE", "EMPLOYEE.LIFECYCLE");
        stubResource("EMPLOYEE.LIFECYCLE", "EMPLOYEE");
        stubResource("EMPLOYEE", null);
        stubPolicy("HR_MANAGER", "EMPLOYEE.LIFECYCLE.TERMINATE", "NONE", PolicyEffect.DENY, PropagationMode.THIS_RESOURCE_ONLY, true);
        lenient().when(policyRepository.findByRoleCodeAndResourceCode("HR_MANAGER", "EMPLOYEE.LIFECYCLE"))
            .thenReturn(Optional.of(new RoleResourcePolicy(
                "HR_MANAGER",
                "EMPLOYEE.LIFECYCLE",
                "WORKFLOW_EXECUTOR",
                PolicyEffect.ALLOW,
                PropagationMode.THIS_RESOURCE_AND_CHILDREN,
                true
            )));

        PermissionDecision decision = service.evaluate(command(List.of("HR_MANAGER"), "EMPLOYEE.LIFECYCLE.TERMINATE", "EXECUTE"));

        assertFalse(decision.isAllowed());
        assertTrue(decision.reason().contains("explicit DENY"));
        assertTrue(decision.reason().contains("EMPLOYEE.LIFECYCLE.TERMINATE"));
        assertTrue(decision.reason().contains("exact resource policy"));
    }

    @Test
    void inheritedDenyFromParentOverridesChildAccess() {
        stubResource("EMPLOYEE.LIFECYCLE.TERMINATE", "EMPLOYEE.LIFECYCLE");
        stubResource("EMPLOYEE.LIFECYCLE", "EMPLOYEE");
        stubResource("EMPLOYEE", null);
        when(policyRepository.findByRoleCodeAndResourceCode("HR_OPERATOR", "EMPLOYEE.LIFECYCLE.TERMINATE"))
                .thenReturn(Optional.empty());
        stubPolicy("HR_OPERATOR", "EMPLOYEE.LIFECYCLE", "NONE", PolicyEffect.DENY, PropagationMode.THIS_RESOURCE_AND_CHILDREN, true);

        PermissionDecision decision = service.evaluate(command(List.of("HR_OPERATOR"), "EMPLOYEE.LIFECYCLE.TERMINATE", "EXECUTE"));

        assertFalse(decision.isAllowed());
        assertTrue(decision.reason().contains("explicit DENY"));
        assertTrue(decision.reason().contains("EMPLOYEE.LIFECYCLE"));
        assertTrue(decision.reason().contains("inherited ancestor policy"));
    }

    @Test
    void explicitDenyFromOneRoleOverridesAllowFromAnotherRole() {
        stubResource("EMPLOYEE.CONTACT", "EMPLOYEE");
        stubResource("EMPLOYEE", null);
        when(policyRepository.findByRoleCodeAndResourceCode("AUDITOR", "EMPLOYEE.CONTACT"))
            .thenReturn(Optional.empty());
        stubPolicy("AUDITOR", "EMPLOYEE", "READ_ONLY", PolicyEffect.ALLOW, PropagationMode.THIS_RESOURCE_AND_CHILDREN, true);
        stubProfile("READ_ONLY", Set.of("READ"));
        stubPolicy("HR_RESTRICTED", "EMPLOYEE.CONTACT", "NONE", PolicyEffect.DENY, PropagationMode.THIS_RESOURCE_ONLY, true);

        PermissionDecision decision = service.evaluate(command(List.of("AUDITOR", "HR_RESTRICTED"), "EMPLOYEE.CONTACT", "READ"));

        assertFalse(decision.isAllowed());
        assertTrue(decision.reason().contains("HR_RESTRICTED"));
        assertTrue(decision.reason().contains("explicit DENY"));
    }

    @Test
    void deniesWhenSecuredResourceNotFound() {
        when(securedResourceRepository.findByCode("UNKNOWN.RESOURCE"))
                .thenReturn(Optional.empty());

        PermissionDecision decision = service.evaluate(command(List.of("HR_MANAGER"), "UNKNOWN.RESOURCE", "READ"));

        assertFalse(decision.isAllowed());
        assertTrue(decision.reason().contains("secured resource 'UNKNOWN.RESOURCE' was not found"));
    }

    @Test
    void deniesWhenSecuredResourceIsInactive() {
        when(securedResourceRepository.findByCode("EMPLOYEE.CONTACT"))
                .thenReturn(Optional.of(new SecuredResource(
                        "EMPLOYEE.CONTACT", "EMPLOYEE", "employee", SecuredResourceKind.VERTICAL, "EMPLOYEE_DATA", "EMPLOYEE.CONTACT", false
                )));

        PermissionDecision decision = service.evaluate(command(List.of("HR_MANAGER"), "EMPLOYEE.CONTACT", "READ"));

        assertFalse(decision.isAllowed());
        assertTrue(decision.reason().contains("secured resource 'EMPLOYEE.CONTACT' is inactive"));
    }

    @Test
    void throwsWhenResourceCodeIsBlank() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.evaluate(new EvaluatePermissionCommand("user1", List.of("HR_MANAGER"), " ", "READ"))
        );
    }

    @Test
    void normalizesCodesBeforeEvaluation() {
        stubResource("EMPLOYEE.CONTACT", "EMPLOYEE");
        stubResource("EMPLOYEE", null);
        stubPolicy("HR_MANAGER", "EMPLOYEE.CONTACT", "SLOT_MAINTAINER", PolicyEffect.ALLOW, PropagationMode.THIS_RESOURCE_ONLY, true);
        stubProfile("SLOT_MAINTAINER", Set.of("READ", "CREATE", "UPDATE", "DELETE"));

        PermissionDecision decision = service.evaluate(
                new EvaluatePermissionCommand("user1", List.of("hr_manager"), "employee.contact", "read")
        );

        assertTrue(decision.isAllowed());
    }

    private EvaluatePermissionCommand command(List<String> roleCodes, String resourceCode, String actionCode) {
        return new EvaluatePermissionCommand("subject1", roleCodes, resourceCode, actionCode);
    }

    private void stubResource(String code, String parentCode) {
        when(securedResourceRepository.findByCode(code))
                .thenReturn(Optional.of(new SecuredResource(
                        code, parentCode, "employee", SecuredResourceKind.VERTICAL, "EMPLOYEE_DATA", code, true
                )));
    }

    private void stubPolicy(
            String roleCode,
            String resourceCode,
            String profileCode,
            PolicyEffect effect,
            PropagationMode mode,
            boolean active
    ) {
        when(policyRepository.findByRoleCodeAndResourceCode(roleCode, resourceCode))
                .thenReturn(Optional.of(new RoleResourcePolicy(roleCode, resourceCode, profileCode, effect, mode, active)));
    }

    private void stubProfile(String code, Set<String> actions) {
        when(profileRepository.findByCode(code))
                .thenReturn(Optional.of(new PermissionProfile(code, code, actions)));
    }
}
