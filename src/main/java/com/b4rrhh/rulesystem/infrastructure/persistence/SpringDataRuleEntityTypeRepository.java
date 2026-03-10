package com.b4rrhh.rulesystem.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataRuleEntityTypeRepository extends JpaRepository<RuleEntityTypeEntity, Long> {
    Optional<RuleEntityTypeEntity> findByCode(String code);
}
