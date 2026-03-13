package com.b4rrhh.employee.presence.application.usecase;

import com.b4rrhh.employee.presence.application.port.EmployeePresenceContext;
import com.b4rrhh.employee.presence.application.port.EmployeePresenceLookupPort;
import com.b4rrhh.employee.presence.application.service.PresenceCatalogValidator;
import com.b4rrhh.employee.presence.domain.exception.ActivePresenceAlreadyExistsException;
import com.b4rrhh.employee.presence.domain.exception.PresenceEmployeeNotFoundException;
import com.b4rrhh.employee.presence.domain.exception.PresenceOverlapException;
import com.b4rrhh.employee.presence.domain.exception.PresenceRuleSystemNotFoundException;
import com.b4rrhh.employee.presence.domain.model.Presence;
import com.b4rrhh.employee.presence.domain.port.PresenceRepository;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreatePresenceService implements CreatePresenceUseCase {

    private final PresenceRepository presenceRepository;
    private final EmployeePresenceLookupPort employeePresenceLookupPort;
    private final RuleSystemRepository ruleSystemRepository;
    private final PresenceCatalogValidator presenceCatalogValidator;

    public CreatePresenceService(
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
    @Transactional
    public Presence create(CreatePresenceCommand command) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(command.ruleSystemCode());
        String normalizedEmployeeTypeCode = normalizeEmployeeTypeCode(command.employeeTypeCode());
        String normalizedEmployeeNumber = normalizeEmployeeNumber(command.employeeNumber());

        ruleSystemRepository.findByCode(normalizedRuleSystemCode)
            .orElseThrow(() -> new PresenceRuleSystemNotFoundException(normalizedRuleSystemCode));

        // Serializes create flow per employee business key to avoid races in overlap/active/max+1 checks.
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

        String companyCode = presenceCatalogValidator.normalizeRequiredCode("companyCode", command.companyCode());
        String entryReasonCode = presenceCatalogValidator.normalizeRequiredCode("entryReasonCode", command.entryReasonCode());
        String exitReasonCode = presenceCatalogValidator.normalizeOptionalCode(command.exitReasonCode());

        presenceCatalogValidator.validateCompanyCode(normalizedRuleSystemCode, companyCode, command.startDate());
        presenceCatalogValidator.validateEntryReasonCode(normalizedRuleSystemCode, entryReasonCode, command.startDate());
        if (exitReasonCode != null) {
            presenceCatalogValidator.validateExitReasonCode(normalizedRuleSystemCode, exitReasonCode, command.startDate());
        }

        if (command.endDate() == null && presenceRepository.existsActivePresence(employee.employeeId())) {
            throw new ActivePresenceAlreadyExistsException(
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber
            );
        }

        if (presenceRepository.existsOverlappingPeriod(employee.employeeId(), command.startDate(), command.endDate())) {
            throw new PresenceOverlapException(
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber
            );
        }

        int nextPresenceNumber = presenceRepository.findMaxPresenceNumberByEmployeeId(employee.employeeId())
                .map(value -> value + 1)
                .orElse(1);

        Presence newPresence = new Presence(
                null,
            employee.employeeId(),
                nextPresenceNumber,
                companyCode,
                entryReasonCode,
                exitReasonCode,
                command.startDate(),
                command.endDate(),
                null,
                null
        );

        return presenceRepository.save(newPresence);
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
