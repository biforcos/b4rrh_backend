package com.b4rrhh.employee.employee.application.usecase;

import com.b4rrhh.employee.employee.domain.exception.EmployeeNotFoundException;
import com.b4rrhh.employee.employee.domain.exception.EmployeeRuleSystemNotFoundException;
import com.b4rrhh.employee.employee.domain.model.Employee;
import com.b4rrhh.employee.employee.domain.port.EmployeeRepository;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateEmployeeService implements UpdateEmployeeUseCase {

    private final EmployeeRepository employeeRepository;
    private final RuleSystemRepository ruleSystemRepository;

    public UpdateEmployeeService(
            EmployeeRepository employeeRepository,
            RuleSystemRepository ruleSystemRepository
    ) {
        this.employeeRepository = employeeRepository;
        this.ruleSystemRepository = ruleSystemRepository;
    }

    @Override
    @Transactional
    public Employee update(UpdateEmployeeCommand command) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(command.ruleSystemCode());
        String normalizedEmployeeTypeCode = normalizeEmployeeTypeCode(command.employeeTypeCode());
        String normalizedEmployeeNumber = normalizeEmployeeNumber(command.employeeNumber());

        String normalizedFirstName = normalizeRequiredField("firstName", command.firstName(), 100);
        String normalizedLastName1 = normalizeRequiredField("lastName1", command.lastName1(), 100);
        String normalizedLastName2 = normalizeOptionalField("lastName2", command.lastName2(), 100);
        String normalizedPreferredName = normalizeOptionalField("preferredName", command.preferredName(), 300);

        ruleSystemRepository.findByCode(normalizedRuleSystemCode)
                .orElseThrow(() -> new EmployeeRuleSystemNotFoundException(normalizedRuleSystemCode));

        Employee existing = employeeRepository.findByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumber(
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber
        ).orElseThrow(() -> new EmployeeNotFoundException(
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber
        ));

        Employee updated = existing.updateIdentityFields(
                normalizedFirstName,
                normalizedLastName1,
                normalizedLastName2,
                normalizedPreferredName
        );

        return employeeRepository.save(updated);
    }

    private String normalizeRuleSystemCode(String ruleSystemCode) {
        if (ruleSystemCode == null || ruleSystemCode.trim().isEmpty()) {
            throw new IllegalArgumentException("ruleSystemCode is required");
        }

        return ruleSystemCode.trim().toUpperCase();
    }

    private String normalizeEmployeeTypeCode(String employeeTypeCode) {
        if (employeeTypeCode == null || employeeTypeCode.trim().isEmpty()) {
            throw new IllegalArgumentException("employeeTypeCode is required");
        }

        return employeeTypeCode.trim().toUpperCase();
    }

    private String normalizeEmployeeNumber(String employeeNumber) {
        if (employeeNumber == null || employeeNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("employeeNumber is required");
        }

        return employeeNumber.trim();
    }

    private String normalizeRequiredField(String fieldName, String value, int maxLength) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " exceeds max length " + maxLength);
        }

        return normalized;
    }

    private String normalizeOptionalField(String fieldName, String value, int maxLength) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }

        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " exceeds max length " + maxLength);
        }

        return normalized;
    }
}