package com.b4rrhh.authorization.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataPermissionProfileRepository extends JpaRepository<PermissionProfileEntity, String> {

    Optional<PermissionProfileEntity> findByCodeAndActiveTrue(String code);
}
