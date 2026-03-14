package com.b4rrhh.employee.identifier.application.usecase;

import com.b4rrhh.employee.identifier.application.port.EmployeeIdentifierContext;
import com.b4rrhh.employee.identifier.application.port.EmployeeIdentifierLookupPort;
import com.b4rrhh.employee.identifier.application.service.IdentifierCatalogValidator;
import com.b4rrhh.employee.identifier.domain.exception.IdentifierAlreadyExistsException;
import com.b4rrhh.employee.identifier.domain.exception.IdentifierEmployeeNotFoundException;
import com.b4rrhh.employee.identifier.domain.exception.IdentifierPrimaryAlreadyExistsException;
import com.b4rrhh.employee.identifier.domain.exception.IdentifierRuleSystemNotFoundException;
import com.b4rrhh.employee.identifier.domain.model.Identifier;
import com.b4rrhh.employee.identifier.domain.port.IdentifierRepository;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class CreateIdentifierService implements CreateIdentifierUseCase {

    private final IdentifierRepository identifierRepository;
    private final EmployeeIdentifierLookupPort employeeIdentifierLookupPort;
    private final RuleSystemRepository ruleSystemRepository;
    private final IdentifierCatalogValidator identifierCatalogValidator;

    public CreateIdentifierService(
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
    public Identifier create(CreateIdentifierCommand command) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(command.ruleSystemCode());
        String normalizedEmployeeTypeCode = normalizeEmployeeTypeCode(command.employeeTypeCode());
        String normalizedEmployeeNumber = normalizeEmployeeNumber(command.employeeNumber());

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

        String identifierTypeCode = identifierCatalogValidator.normalizeRequiredCode(
                "identifierTypeCode",
                command.identifierTypeCode()
        );
        identifierCatalogValidator.validateIdentifierTypeCode(normalizedRuleSystemCode, identifierTypeCode, LocalDate.now());

        identifierRepository.findByEmployeeIdAndIdentifierTypeCode(employee.employeeId(), identifierTypeCode)
                .ifPresent(existing -> {
                    throw new IdentifierAlreadyExistsException(
                            normalizedRuleSystemCode,
                            normalizedEmployeeTypeCode,
                            normalizedEmployeeNumber,
                            identifierTypeCode
                    );
                });

        Identifier newIdentifier = new Identifier(
                null,
                employee.employeeId(),
                identifierTypeCode,
                command.identifierValue(),
                command.issuingCountryCode(),
                command.expirationDate(),
                command.isPrimary(),
                null,
                null
        );

        if (newIdentifier.isPrimary() && identifierRepository.existsByEmployeeIdAndIsPrimaryTrue(employee.employeeId())) {
            throw new IdentifierPrimaryAlreadyExistsException(
                    normalizedRuleSystemCode,
                    normalizedEmployeeTypeCode,
                    normalizedEmployeeNumber
            );
        }

        return identifierRepository.save(newIdentifier);
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
