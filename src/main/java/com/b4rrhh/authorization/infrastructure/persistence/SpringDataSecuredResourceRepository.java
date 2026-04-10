package com.b4rrhh.authorization.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataSecuredResourceRepository extends JpaRepository<SecuredResourceEntity, String> {

    Optional<SecuredResourceEntity> findByCode(String code);
}
