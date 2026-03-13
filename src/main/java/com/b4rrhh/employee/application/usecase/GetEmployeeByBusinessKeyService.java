package com.b4rrhh.employee.application.usecase;

import com.b4rrhh.employee.domain.exception.EmployeeRuleSystemNotFoundException;
import com.b4rrhh.employee.domain.model.Employee;
import com.b4rrhh.employee.domain.port.EmployeeRepository;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetEmployeeByBusinessKeyService implements GetEmployeeByBusinessKeyUseCase {

    private final EmployeeRepository employeeRepository;
    private final RuleSystemRepository ruleSystemRepository;

    public GetEmployeeByBusinessKeyService(
            EmployeeRepository employeeRepository,
            RuleSystemRepository ruleSystemRepository
    ) {
        this.employeeRepository = employeeRepository;
        this.ruleSystemRepository = ruleSystemRepository;
    }

    @Override
    public Optional<Employee> getByBusinessKey(String ruleSystemCode, String employeeTypeCode, String employeeNumber) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(ruleSystemCode);
        String normalizedEmployeeTypeCode = normalizeEmployeeTypeCode(employeeTypeCode);
        String normalizedEmployeeNumber = normalizeEmployeeNumber(employeeNumber);

        ruleSystemRepository.findByCode(normalizedRuleSystemCode)
                .orElseThrow(() -> new EmployeeRuleSystemNotFoundException(normalizedRuleSystemCode));

        return employeeRepository.findByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumber(
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber
        );
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
}
