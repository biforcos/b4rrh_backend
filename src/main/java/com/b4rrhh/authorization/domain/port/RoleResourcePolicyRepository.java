package com.b4rrhh.authorization.domain.port;

import com.b4rrhh.authorization.domain.model.RoleResourcePolicy;

import java.util.Optional;

public interface RoleResourcePolicyRepository {

    Optional<RoleResourcePolicy> findByRoleCodeAndResourceCode(String roleCode, String resourceCode);
}
