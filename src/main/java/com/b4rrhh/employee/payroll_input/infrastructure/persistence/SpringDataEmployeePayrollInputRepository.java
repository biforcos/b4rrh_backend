package com.b4rrhh.employee.payroll_input.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataEmployeePayrollInputRepository
        extends JpaRepository<EmployeePayrollInputEntity, Long> {

    boolean existsByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumberAndConceptCodeAndPeriod(
            String ruleSystemCode, String employeeTypeCode,
            String employeeNumber, String conceptCode, int period);

    Optional<EmployeePayrollInputEntity> findByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumberAndConceptCodeAndPeriod(
            String ruleSystemCode, String employeeTypeCode,
            String employeeNumber, String conceptCode, int period);

    List<EmployeePayrollInputEntity> findByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumberAndPeriodOrderByConceptCode(
            String ruleSystemCode, String employeeTypeCode,
            String employeeNumber, int period);

    void deleteByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumberAndConceptCodeAndPeriod(
            String ruleSystemCode, String employeeTypeCode,
            String employeeNumber, String conceptCode, int period);
}
