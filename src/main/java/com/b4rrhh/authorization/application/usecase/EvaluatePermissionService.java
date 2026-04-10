package com.b4rrhh.authorization.application.usecase;

import com.b4rrhh.authorization.domain.model.PermissionDecision;
import com.b4rrhh.authorization.domain.model.PermissionProfile;
import com.b4rrhh.authorization.domain.model.PolicyEffect;
import com.b4rrhh.authorization.domain.model.PropagationMode;
import com.b4rrhh.authorization.domain.model.RoleResourcePolicy;
import com.b4rrhh.authorization.domain.model.SecuredResource;
import com.b4rrhh.authorization.domain.port.PermissionProfileRepository;
import com.b4rrhh.authorization.domain.port.RoleResourcePolicyRepository;
import com.b4rrhh.authorization.domain.port.SecuredResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EvaluatePermissionService implements EvaluatePermissionUseCase {

    private final SecuredResourceRepository securedResourceRepository;
    private final RoleResourcePolicyRepository policyRepository;
    private final PermissionProfileRepository profileRepository;

    public EvaluatePermissionService(
            SecuredResourceRepository securedResourceRepository,
            RoleResourcePolicyRepository policyRepository,
            PermissionProfileRepository profileRepository
    ) {
        this.securedResourceRepository = securedResourceRepository;
        this.policyRepository = policyRepository;
        this.profileRepository = profileRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionDecision evaluate(EvaluatePermissionCommand command) {
        String resourceCode = normalizeCode("resourceCode", command.resourceCode());
        String actionCode = normalizeCode("actionCode", command.actionCode());

        ResolvedAncestorPath resolvedAncestorPath = resolveAncestorPath(resourceCode);
        if (!resolvedAncestorPath.resolved()) {
            return PermissionDecision.deny(resolvedAncestorPath.failureReason());
        }

        if (command.roleCodes() == null || command.roleCodes().isEmpty()) {
            return PermissionDecision.deny(
                    "Denied because subject '" + command.subject() + "' has no roles to evaluate action '"
                            + actionCode + "' on resource '" + resourceCode + "'"
            );
        }

        RoleEvaluation allowDecision = null;
        RoleEvaluation fallbackDenyDecision = null;

        for (String rawRoleCode : command.roleCodes()) {
            String roleCode = normalizeCode("roleCode", rawRoleCode);
            RoleEvaluation roleEvaluation = evaluateRole(
                    command.subject(),
                    roleCode,
                    resourceCode,
                    resolvedAncestorPath.ancestorPath(),
                    actionCode
            );

            if (roleEvaluation.explicitDeny()) {
                return PermissionDecision.deny(roleEvaluation.reason());
            }
            if (roleEvaluation.allowed()) {
                if (allowDecision == null) {
                    allowDecision = roleEvaluation;
                }
                continue;
            }
            if (fallbackDenyDecision == null) {
                fallbackDenyDecision = roleEvaluation;
            }
        }

        if (allowDecision != null) {
            return PermissionDecision.allow(allowDecision.reason());
        }
        if (fallbackDenyDecision != null) {
            return PermissionDecision.deny(fallbackDenyDecision.reason());
        }

        return PermissionDecision.deny(
                "Denied because no evaluated role grants action '" + actionCode + "' on resource '"
                        + resourceCode + "' for subject '" + command.subject() + "'"
        );
    }

    private ResolvedAncestorPath resolveAncestorPath(String resourceCode) {
        List<SecuredResource> path = new ArrayList<>();
        String current = resourceCode;
        while (current != null) {
            String lookup = current;
            Optional<SecuredResource> resourceOpt = securedResourceRepository.findByCode(lookup);
            if (resourceOpt.isEmpty()) {
                return ResolvedAncestorPath.denied(
                        "Denied because secured resource '" + lookup + "' was not found"
                );
            }
            SecuredResource resource = resourceOpt.get();
            if (!resource.active()) {
                return ResolvedAncestorPath.denied(
                        "Denied because secured resource '" + lookup + "' is inactive"
                );
            }
            path.add(resource);
            current = resource.parentCode();
        }
        return ResolvedAncestorPath.resolved(path);
    }

    private RoleEvaluation evaluateRole(
            String subject,
            String roleCode,
            String requestedResourceCode,
            List<SecuredResource> ancestorPath,
            String actionCode
    ) {
        for (int i = 0; i < ancestorPath.size(); i++) {
            SecuredResource node = ancestorPath.get(i);
            boolean isExactResource = (i == 0);

            Optional<RoleResourcePolicy> policyOpt = policyRepository.findByRoleCodeAndResourceCode(roleCode, node.code());
            if (policyOpt.isEmpty()) {
                continue;
            }

            RoleResourcePolicy policy = policyOpt.get();
            if (!policy.active()) {
                continue;
            }

            boolean applies = isExactResource || policy.propagationMode() == PropagationMode.THIS_RESOURCE_AND_CHILDREN;
            if (!applies) {
                continue;
            }

            if (policy.effect() == PolicyEffect.DENY) {
                return RoleEvaluation.explicitDeny(
                        "Denied because role '" + roleCode + "' has an explicit DENY policy on resource '"
                                + node.code() + "' that applies to requested resource '" + requestedResourceCode + "'"
                                + policyOriginDescription(isExactResource, node.code())
                );
            }

            Optional<PermissionProfile> profileOpt = profileRepository.findByCode(policy.permissionProfileCode());
            if (profileOpt.isEmpty()) {
                return RoleEvaluation.deny(
                        "Denied because permission profile '" + policy.permissionProfileCode()
                                + "' was not found for role '" + roleCode + "' on resource '" + node.code() + "'"
                                + policyOriginDescription(isExactResource, node.code())
                );
            }

            PermissionProfile profile = profileOpt.get();
            if (!profile.containsAction(actionCode)) {
                return RoleEvaluation.deny(
                        "Denied because action '" + actionCode + "' is not granted by permission profile '"
                                + profile.code() + "' for role '" + roleCode + "' on resource '" + node.code() + "'"
                                + policyOriginDescription(isExactResource, node.code())
                );
            }

            return RoleEvaluation.allow(
                    "Granted to subject '" + subject + "' by role '" + roleCode + "' using profile '"
                            + profile.code() + "' on resource '" + node.code() + "'"
                            + policyOriginDescription(isExactResource, node.code())
            );
        }

        return RoleEvaluation.deny(
                "Denied because no applicable policy was found for role '" + roleCode
                        + "' on resource '" + requestedResourceCode + "'"
        );
    }

    private String normalizeCode(String fieldName, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim().toUpperCase();
    }

    private String policyOriginDescription(boolean exactResource, String policyResourceCode) {
        if (exactResource) {
            return " from exact resource policy";
        }
        return " from inherited ancestor policy '" + policyResourceCode + "'";
    }

    private record ResolvedAncestorPath(List<SecuredResource> ancestorPath, String failureReason) {

        private static ResolvedAncestorPath resolved(List<SecuredResource> ancestorPath) {
            return new ResolvedAncestorPath(List.copyOf(ancestorPath), null);
        }

        private static ResolvedAncestorPath denied(String failureReason) {
            return new ResolvedAncestorPath(List.of(), failureReason);
        }

        private boolean resolved() {
            return failureReason == null;
        }
    }

    private record RoleEvaluation(boolean allowed, boolean explicitDeny, String reason) {

        private static RoleEvaluation allow(String reason) {
            return new RoleEvaluation(true, false, reason);
        }

        private static RoleEvaluation deny(String reason) {
            return new RoleEvaluation(false, false, reason);
        }

        private static RoleEvaluation explicitDeny(String reason) {
            return new RoleEvaluation(false, true, reason);
        }
    }
}
