package com.b4rrhh.employee.presence.application.usecase;

import com.b4rrhh.employee.presence.application.port.EmployeePresenceContext;
import com.b4rrhh.employee.presence.application.port.EmployeePresenceLookupPort;
import com.b4rrhh.employee.presence.application.service.PresenceCatalogValidator;
import com.b4rrhh.employee.presence.domain.exception.PresenceEmployeeNotFoundException;
import com.b4rrhh.employee.presence.domain.exception.PresenceNotFoundException;
import com.b4rrhh.employee.presence.domain.exception.PresenceRuleSystemNotFoundException;
import com.b4rrhh.employee.presence.domain.model.Presence;
import com.b4rrhh.employee.presence.domain.port.PresenceRepository;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.springframework.stereotype.Service;

@Service
public class ClosePresenceService implements ClosePresenceUseCase {

    private final PresenceRepository presenceRepository;
    private final EmployeePresenceLookupPort employeePresenceLookupPort;
    private final RuleSystemRepository ruleSystemRepository;
    private final PresenceCatalogValidator presenceCatalogValidator;

    public ClosePresenceService(
            PresenceRepository presenceRepository,
            EmployeePresenceLookupPort employeePresenceLookupPort,
            RuleSystemRepository ruleSystemRepository,
            PresenceCatalogValidator presenceCatalogValidator
    ) {
        this.presenceRepository = presenceRepository;
        this.employeePresenceLookupPort = employeePresenceLookupPort;
        this.ruleSystemRepository = ruleSystemRepository;
        this.presenceCatalogValidator = presenceCatalogValidator;
    }

    @Override
    public Presence close(ClosePresenceCommand command) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(command.ruleSystemCode());
        String normalizedEmployeeTypeCode = normalizeEmployeeTypeCode(command.employeeTypeCode());
        String normalizedEmployeeNumber = normalizeEmployeeNumber(command.employeeNumber());
        Integer normalizedPresenceNumber = normalizePresenceNumber(command.presenceNumber());

        ruleSystemRepository.findByCode(normalizedRuleSystemCode)
                .orElseThrow(() -> new PresenceRuleSystemNotFoundException(normalizedRuleSystemCode));

        EmployeePresenceContext employee = employeePresenceLookupPort
                .findByBusinessKeyForUpdate(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                )
                .orElseThrow(() -> new PresenceEmployeeNotFoundException(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                ));

        Presence existing = presenceRepository
                .findByEmployeeIdAndPresenceNumber(employee.employeeId(), normalizedPresenceNumber)
                .orElseThrow(() -> new PresenceNotFoundException(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber,
                        normalizedPresenceNumber
                ));

        String normalizedExitReasonCode = presenceCatalogValidator.normalizeOptionalCode(command.exitReasonCode());
        if (normalizedExitReasonCode != null) {
            presenceCatalogValidator.validateExitReasonCode(normalizedRuleSystemCode, normalizedExitReasonCode, command.endDate());
        }

        Presence closed = existing.close(command.endDate(), normalizedExitReasonCode);
        return presenceRepository.save(closed);
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
