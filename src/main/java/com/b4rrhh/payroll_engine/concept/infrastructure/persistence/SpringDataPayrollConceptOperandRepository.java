package com.b4rrhh.payroll_engine.concept.infrastructure.persistence;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataPayrollConceptOperandRepository
        extends JpaRepository<PayrollConceptOperandEntity, Long> {

    List<PayrollConceptOperandEntity> findByTargetObject_RuleSystemCodeAndTargetObject_ObjectCode(
            String ruleSystemCode, String objectCode);

    @Query("""
            select o from PayrollEngineConceptOperandEntity o
            where o.targetObject.ruleSystemCode = :ruleSystemCode
              and o.targetObject.objectCode = :conceptCode
            order by o.operandRole asc
            """)
    List<PayrollConceptOperandEntity> findByRuleSystemCodeAndConceptCode(
            @Param("ruleSystemCode") String ruleSystemCode,
            @Param("conceptCode") String conceptCode
    );

    @Modifying
    @Transactional
    @Query("""
            delete from PayrollEngineConceptOperandEntity o
            where o.targetObject.ruleSystemCode = :ruleSystemCode
              and o.targetObject.objectCode = :conceptCode
            """)
    void deleteAllByRuleSystemCodeAndConceptCode(
            @Param("ruleSystemCode") String ruleSystemCode,
            @Param("conceptCode") String conceptCode
    );
}
