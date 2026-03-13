package com.b4rrhh.employee.employee.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SpringDataEmployeeRepository extends JpaRepository<EmployeeEntity, Long> {

    Optional<EmployeeEntity> findByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumber(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );
}