package com.b4rrhh.authorization.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataPermissionProfileActionRepository
        extends JpaRepository<PermissionProfileActionEntity, PermissionProfileActionEntity.Pk> {

    List<PermissionProfileActionEntity> findByPermissionProfileCode(String permissionProfileCode);
}
