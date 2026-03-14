package com.b4rrhh.employee.identifier.application.usecase;

import com.b4rrhh.employee.identifier.application.port.EmployeeIdentifierContext;
import com.b4rrhh.employee.identifier.application.port.EmployeeIdentifierLookupPort;
import com.b4rrhh.employee.identifier.domain.exception.IdentifierEmployeeNotFoundException;
import com.b4rrhh.employee.identifier.domain.exception.IdentifierNotFoundException;
import com.b4rrhh.employee.identifier.domain.exception.IdentifierRuleSystemNotFoundException;
import com.b4rrhh.employee.identifier.domain.model.Identifier;
import com.b4rrhh.employee.identifier.domain.port.IdentifierRepository;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteIdentifierService implements DeleteIdentifierUseCase {

    private final IdentifierRepository identifierRepository;
    private final EmployeeIdentifierLookupPort employeeIdentifierLookupPort;
    private final RuleSystemRepository ruleSystemRepository;

    public DeleteIdentifierService(
            IdentifierRepository identifierRepository,
            EmployeeIdentifierLookupPort employeeIdentifierLookupPort,
            RuleSystemRepository ruleSystemRepository
    ) {
        this.identifierRepository = identifierRepository;
        this.employeeIdentifierLookupPort = employeeIdentifierLookupPort;
        this.ruleSystemRepository = ruleSystemRepository;
    }

    @Override
    @Transactional
    public void delete(DeleteIdentifierCommand command) {
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

        identifierRepository.deleteByEmployeeIdAndIdentifierTypeCode(
                existing.getEmployeeId(),
                existing.getIdentifierTypeCode()
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

    private String normalizeIdentifierTypeCode(String identifierTypeCode) {
        if (identifierTypeCode == null || identifierTypeCode.trim().isEmpty()) {
            throw new IllegalArgumentException("identifierTypeCode is required");
        }

        return identifierTypeCode.trim().toUpperCase();
    }
}
