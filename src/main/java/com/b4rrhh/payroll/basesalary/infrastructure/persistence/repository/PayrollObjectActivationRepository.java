package com.b4rrhh.payroll.basesalary.infrastructure.persistence.repository;

import com.b4rrhh.payroll.basesalary.infrastructure.persistence.entity.PayrollObjectActivationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollObjectActivationRepository extends JpaRepository<PayrollObjectActivationEntity, Long> {

    boolean existsByRuleSystemCodeAndOwnerTypeCodeAndOwnerCodeAndTargetObjectTypeCodeAndTargetObjectCodeAndActiveTrue(
            String ruleSystemCode,
            String ownerTypeCode,
            String ownerCode,
            String targetObjectTypeCode,
            String targetObjectCode
    );
}
