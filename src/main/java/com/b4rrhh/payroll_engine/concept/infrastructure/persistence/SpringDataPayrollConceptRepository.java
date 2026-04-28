package com.b4rrhh.payroll_engine.concept.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SpringDataPayrollConceptRepository extends JpaRepository<PayrollConceptEntity, Long> {

    @Query("""
        select c from PayrollEngineConceptEntity c
        where c.payrollObject.ruleSystemCode = :ruleSystemCode
          and c.payrollObject.objectCode = :conceptCode
          and c.payrollObject.objectTypeCode = 'CONCEPT'
        """)
    Optional<PayrollConceptEntity> findByRuleSystemCodeAndConceptCode(
            @Param("ruleSystemCode") String ruleSystemCode,
            @Param("conceptCode") String conceptCode
    );

    @Query("""
        select (count(c) > 0) from PayrollEngineConceptEntity c
        where c.payrollObject.ruleSystemCode = :ruleSystemCode
          and c.payrollObject.objectCode = :conceptCode
          and c.payrollObject.objectTypeCode = 'CONCEPT'
        """)
    boolean existsByRuleSystemCodeAndConceptCode(
            @Param("ruleSystemCode") String ruleSystemCode,
            @Param("conceptCode") String conceptCode
    );

    @Query("""
        select c from PayrollEngineConceptEntity c
        where c.payrollObject.ruleSystemCode = :ruleSystemCode
          and c.payrollObject.objectCode in :conceptCodes
          and c.payrollObject.objectTypeCode = 'CONCEPT'
        """)
    List<PayrollConceptEntity> findAllByRuleSystemCodeAndConceptCodes(
            @Param("ruleSystemCode") String ruleSystemCode,
            @Param("conceptCodes") Collection<String> conceptCodes
    );

    @Query("""
        select c from PayrollEngineConceptEntity c
        where c.payrollObject.ruleSystemCode = :ruleSystemCode
          and c.payrollObject.objectTypeCode = 'CONCEPT'
        order by c.payrollObject.objectCode
        """)
    List<PayrollConceptEntity> findAllByRuleSystemCode(
            @Param("ruleSystemCode") String ruleSystemCode
    );

    @Modifying
    @Query("""
        delete from PayrollEngineConceptEntity c
        where c.payrollObject.ruleSystemCode = :ruleSystemCode
          and c.payrollObject.objectCode = :conceptCode
          and c.payrollObject.objectTypeCode = 'CONCEPT'
        """)
    void deleteByRuleSystemCodeAndConceptCode(
            @Param("ruleSystemCode") String ruleSystemCode,
            @Param("conceptCode") String conceptCode
    );
}
