package com.b4rrhh.payroll.basesalary.infrastructure.persistence.repository;

import com.b4rrhh.payroll.basesalary.infrastructure.persistence.entity.PayrollObjectBindingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for PayrollObjectBindingEntity.
 * Persistence port.
 */
public interface PayrollObjectBindingRepository extends JpaRepository<PayrollObjectBindingEntity, Long> {

    /**
     * Find binding by rule system, owner, and binding role.
     */
        Optional<PayrollObjectBindingEntity> findByRuleSystemCodeAndOwnerTypeCodeAndOwnerCodeAndBindingRoleCodeAndBoundObjectTypeCodeAndActiveTrue(
            String ruleSystemCode,
            String ownerTypeCode,
            String ownerCode,
            String bindingRoleCode,
            String boundObjectTypeCode
    );
}
