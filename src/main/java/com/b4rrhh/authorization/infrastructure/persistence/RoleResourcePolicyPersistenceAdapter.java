package com.b4rrhh.authorization.infrastructure.persistence;

import com.b4rrhh.authorization.domain.model.PropagationMode;
import com.b4rrhh.authorization.domain.model.PolicyEffect;
import com.b4rrhh.authorization.domain.model.RoleResourcePolicy;
import com.b4rrhh.authorization.domain.port.RoleResourcePolicyRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RoleResourcePolicyPersistenceAdapter implements RoleResourcePolicyRepository {

    private final SpringDataRoleResourcePolicyRepository springDataRepository;

    public RoleResourcePolicyPersistenceAdapter(SpringDataRoleResourcePolicyRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Optional<RoleResourcePolicy> findByRoleCodeAndResourceCode(String roleCode, String resourceCode) {
        return springDataRepository.findByRoleCodeAndResourceCode(roleCode, resourceCode)
                .map(this::toDomain);
    }

    private RoleResourcePolicy toDomain(RoleResourcePolicyEntity entity) {
        PolicyEffect effect;
        try {
            effect = PolicyEffect.valueOf(entity.getEffect());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "Invalid effect in database for policy ("
                    + entity.getRoleCode() + ", " + entity.getResourceCode()
                    + "): " + entity.getEffect(), e
            );
        }

        PropagationMode propagationMode;
        try {
            propagationMode = PropagationMode.valueOf(entity.getPropagationMode());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "Invalid propagation_mode in database for policy ("
                    + entity.getRoleCode() + ", " + entity.getResourceCode()
                    + "): " + entity.getPropagationMode(), e
            );
        }
        return new RoleResourcePolicy(
                entity.getRoleCode(),
                entity.getResourceCode(),
                entity.getPermissionProfileCode(),
            effect,
                propagationMode,
                entity.isActive()
        );
    }
}
