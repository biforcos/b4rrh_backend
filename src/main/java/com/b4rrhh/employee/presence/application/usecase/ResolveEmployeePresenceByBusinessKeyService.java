package com.b4rrhh.employee.presence.application.usecase;

import com.b4rrhh.employee.presence.application.port.EmployeePresenceContext;
import com.b4rrhh.employee.presence.application.port.EmployeePresenceLookupPort;
import com.b4rrhh.employee.presence.domain.exception.PresenceEmployeeBusinessKeyMismatchException;
import com.b4rrhh.employee.presence.domain.exception.PresenceEmployeeNotFoundException;
import com.b4rrhh.employee.presence.domain.exception.PresenceRuleSystemNotFoundException;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.springframework.stereotype.Service;

@Service
public class ResolveEmployeePresenceByBusinessKeyService implements ResolveEmployeePresenceByBusinessKeyUseCase {

    private final EmployeePresenceLookupPort employeePresenceLookupPort;
    private final RuleSystemRepository ruleSystemRepository;

    public ResolveEmployeePresenceByBusinessKeyService(
            EmployeePresenceLookupPort employeePresenceLookupPort,
            RuleSystemRepository ruleSystemRepository
    ) {
        this.employeePresenceLookupPort = employeePresenceLookupPort;
        this.ruleSystemRepository = ruleSystemRepository;
    }

    @Override
    public EmployeePresenceContext resolve(String ruleSystemCode, String employeeNumber) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(ruleSystemCode);
        String normalizedEmployeeNumber = normalizeEmployeeNumber(employeeNumber);

        ruleSystemRepository.findByCode(normalizedRuleSystemCode)
                .orElseThrow(() -> new PresenceRuleSystemNotFoundException(normalizedRuleSystemCode));

        EmployeePresenceContext employee = employeePresenceLookupPort
                .findByBusinessKey(normalizedRuleSystemCode, normalizedEmployeeNumber)
                .orElseThrow(() -> new PresenceEmployeeNotFoundException(normalizedRuleSystemCode, normalizedEmployeeNumber));

        String employeeRuleSystemCode = employee.ruleSystemCode().trim().toUpperCase();
        if (!employeeRuleSystemCode.equals(normalizedRuleSystemCode)) {
            throw new PresenceEmployeeBusinessKeyMismatchException(
                    normalizedRuleSystemCode,
                    normalizedEmployeeNumber,
                    employeeRuleSystemCode
            );
        }

        return employee;
    }

    private String normalizeRuleSystemCode(String ruleSystemCode) {
        if (ruleSystemCode == null || ruleSystemCode.trim().isEmpty()) {
            throw new IllegalArgumentException("ruleSystemCode is required");
        }

        return ruleSystemCode.trim().toUpperCase();
    }

    private String normalizeEmployeeNumber(String employeeNumber) {
        if (employeeNumber == null || employeeNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("employeeNumber is required");
        }

        return employeeNumber.trim();
    }
}
