package com.b4rrhh.rulesystem.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataRuleSystemRepository extends JpaRepository<RuleSystemEntity, Long> {
    Optional<RuleSystemEntity> findByCode(String code);
}