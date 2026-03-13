package com.b4rrhh.employee.presence.application.usecase;

import com.b4rrhh.employee.presence.application.port.EmployeePresenceContext;
import com.b4rrhh.employee.presence.application.port.EmployeePresenceLookupPort;
import com.b4rrhh.employee.presence.domain.exception.PresenceEmployeeNotFoundException;
import com.b4rrhh.employee.presence.domain.model.Presence;
import com.b4rrhh.employee.presence.domain.port.PresenceRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetPresenceByBusinessKeyService implements GetPresenceByBusinessKeyUseCase {

    private final PresenceRepository presenceRepository;
    private final EmployeePresenceLookupPort employeePresenceLookupPort;

    public GetPresenceByBusinessKeyService(
            PresenceRepository presenceRepository,
            EmployeePresenceLookupPort employeePresenceLookupPort
    ) {
        this.presenceRepository = presenceRepository;
        this.employeePresenceLookupPort = employeePresenceLookupPort;
    }

    @Override
    public Optional<Presence> getByBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            Integer presenceNumber
    ) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(ruleSystemCode);
        String normalizedEmployeeTypeCode = normalizeEmployeeTypeCode(employeeTypeCode);
        String normalizedEmployeeNumber = normalizeEmployeeNumber(employeeNumber);
        Integer normalizedPresenceNumber = normalizePresenceNumber(presenceNumber);

        EmployeePresenceContext employee = employeePresenceLookupPort
                .findByBusinessKey(normalizedRuleSystemCode, normalizedEmployeeTypeCode, normalizedEmployeeNumber)
                .orElseThrow(() -> new PresenceEmployeeNotFoundException(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                ));

        return presenceRepository.findByEmployeeIdAndPresenceNumber(employee.employeeId(), normalizedPresenceNumber);
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

    private Integer normalizePresenceNumber(Integer presenceNumber) {
        if (presenceNumber == null || presenceNumber <= 0) {
            throw new IllegalArgumentException("presenceNumber must be a positive integer");
        }

        return presenceNumber;
    }
}
