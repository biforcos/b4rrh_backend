package com.b4rrhh.authorization.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataRoleResourcePolicyRepository
        extends JpaRepository<RoleResourcePolicyEntity, RoleResourcePolicyEntity.Pk> {

    Optional<RoleResourcePolicyEntity> findByRoleCodeAndResourceCode(String roleCode, String resourceCode);
}
