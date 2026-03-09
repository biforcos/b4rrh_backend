package com.b4rrhh.employee.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SpringDataEmployeeRepository extends JpaRepository<EmployeeEntity, Long> {

    Optional<EmployeeEntity> findByRuleSystemCodeAndEmployeeNumber(String ruleSystemCode, String employeeNumber);
}