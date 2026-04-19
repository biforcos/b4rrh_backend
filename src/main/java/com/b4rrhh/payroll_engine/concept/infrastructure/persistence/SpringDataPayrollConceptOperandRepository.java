package com.b4rrhh.payroll_engine.concept.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataPayrollConceptOperandRepository
        extends JpaRepository<PayrollConceptOperandEntity, Long> {

    List<PayrollConceptOperandEntity> findByTargetObject_RuleSystemCodeAndTargetObject_ObjectCode(
            String ruleSystemCode, String objectCode);
}
