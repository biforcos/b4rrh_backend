package com.b4rrhh.employee.identifier.application.usecase;

import com.b4rrhh.employee.identifier.application.port.EmployeeIdentifierContext;
import com.b4rrhh.employee.identifier.application.port.EmployeeIdentifierLookupPort;
import com.b4rrhh.employee.identifier.application.service.IdentifierCatalogValidator;
import com.b4rrhh.employee.identifier.domain.exception.IdentifierEmployeeNotFoundException;
import com.b4rrhh.employee.identifier.domain.exception.IdentifierNotFoundException;
import com.b4rrhh.employee.identifier.domain.exception.IdentifierPrimaryAlreadyExistsException;
import com.b4rrhh.employee.identifier.domain.exception.IdentifierRuleSystemNotFoundException;
import com.b4rrhh.employee.identifier.domain.model.Identifier;
import com.b4rrhh.employee.identifier.domain.port.IdentifierRepository;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class UpdateIdentifierService implements UpdateIdentifierUseCase {

    private final IdentifierRepository identifierRepository;
    private final EmployeeIdentifierLookupPort employeeIdentifierLookupPort;
    private final RuleSystemRepository ruleSystemRepository;
    private final IdentifierCatalogValidator identifierCatalogValidator;

    public UpdateIdentifierService(
            IdentifierRepository identifierRepository,
            EmployeeIdentifierLookupPort employeeIdentifierLookupPort,
            RuleSystemRepository ruleSystemRepository,
            IdentifierCatalogValidator identifierCatalogValidator
    ) {
        this.identifierRepository = identifierRepository;
        this.employeeIdentifierLookupPort = employeeIdentifierLookupPort;
        this.ruleSystemRepository = ruleSystemRepository;
        this.identifierCatalogValidator = identifierCatalogValidator;
    }

    @Override
    @Transactional
    public Identifier update(UpdateIdentifierCommand command) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(command.ruleSystemCode());
        String normalizedEmployeeTypeCode = normalizeEmployeeTypeCode(command.employeeTypeCode());
        String normalizedEmployeeNumber = normalizeEmployeeNumber(command.employeeNumber());
        String normalizedIdentifierTypeCode = normalizeIdentifierTypeCode(command.identifierTypeCode());

        ruleSystemRepository.findByCode(normalizedRuleSystemCode)
                .orElseThrow(() -> new IdentifierRuleSystemNotFoundException(normalizedRuleSystemCode));

        EmployeeIdentifierContext employee = employeeIdentifierLookupPort
                .findByBusinessKeyForUpdate(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                )
                .orElseThrow(() -> new IdentifierEmployeeNotFoundException(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                ));

        Identifier existing = identifierRepository
                .findByEmployeeIdAndIdentifierTypeCode(employee.employeeId(), normalizedIdentifierTypeCode)
                .orElseThrow(() -> new IdentifierNotFoundException(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber,
                        normalizedIdentifierTypeCode
                ));

        identifierCatalogValidator.validateIdentifierTypeCode(
                normalizedRuleSystemCode,
                normalizedIdentifierTypeCode,
                LocalDate.now()
        );

        Identifier updated = existing.update(
                command.identifierValue(),
                command.issuingCountryCode(),
                command.expirationDate(),
                command.isPrimary()
        );

        if (updated.isPrimary()
                && identifierRepository.existsByEmployeeIdAndIsPrimaryTrueAndIdentifierTypeCodeNot(
                employee.employeeId(),
                normalizedIdentifierTypeCode
        )) {
            throw new IdentifierPrimaryAlreadyExistsException(
                    normalizedRuleSystemCode,
                    normalizedEmployeeTypeCode,
                    normalizedEmployeeNumber
            );
        }

        return identifierRepository.save(updated);
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

    private String normalizeIdentifierTypeCode(String identifierTypeCode) {
        if (identifierTypeCode == null || identifierTypeCode.trim().isEmpty()) {
            throw new IllegalArgumentException("identifierTypeCode is required");
        }

        return identifierTypeCode.trim().toUpperCase();
    }
}
