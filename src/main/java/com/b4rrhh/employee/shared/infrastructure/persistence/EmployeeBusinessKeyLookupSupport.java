package com.b4rrhh.employee.shared.infrastructure.persistence;

import com.b4rrhh.employee.employee.infrastructure.persistence.EmployeeEntity;
import com.b4rrhh.employee.employee.infrastructure.persistence.SpringDataEmployeeRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EmployeeBusinessKeyLookupSupport {

    private final SpringDataEmployeeRepository springDataEmployeeRepository;

    public EmployeeBusinessKeyLookupSupport(SpringDataEmployeeRepository springDataEmployeeRepository) {
        this.springDataEmployeeRepository = springDataEmployeeRepository;
    }

    public Optional<EmployeeEntity> findByBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        return springDataEmployeeRepository.findByBusinessKey(
                ruleSystemCode,
                employeeTypeCode,
                employeeNumber
        );
    }

    public Optional<EmployeeEntity> findByBusinessKeyForUpdate(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        return springDataEmployeeRepository.findByBusinessKeyForUpdate(
                ruleSystemCode,
                employeeTypeCode,
                employeeNumber
        );
    }
}