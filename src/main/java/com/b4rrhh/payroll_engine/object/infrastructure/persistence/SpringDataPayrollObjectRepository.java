package com.b4rrhh.payroll_engine.object.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataPayrollObjectRepository extends JpaRepository<PayrollObjectEntity, Long> {

    Optional<PayrollObjectEntity> findByRuleSystemCodeAndObjectTypeCodeAndObjectCode(
            String ruleSystemCode,
            String objectTypeCode,
            String objectCode
    );

    boolean existsByRuleSystemCodeAndObjectTypeCodeAndObjectCode(
            String ruleSystemCode,
            String objectTypeCode,
            String objectCode
    );
}
