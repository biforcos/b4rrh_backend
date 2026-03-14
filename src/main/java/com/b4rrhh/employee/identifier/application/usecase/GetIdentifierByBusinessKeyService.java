package com.b4rrhh.employee.identifier.application.usecase;

import com.b4rrhh.employee.identifier.application.port.EmployeeIdentifierContext;
import com.b4rrhh.employee.identifier.application.port.EmployeeIdentifierLookupPort;
import com.b4rrhh.employee.identifier.domain.exception.IdentifierEmployeeNotFoundException;
import com.b4rrhh.employee.identifier.domain.model.Identifier;
import com.b4rrhh.employee.identifier.domain.port.IdentifierRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetIdentifierByBusinessKeyService implements GetIdentifierByBusinessKeyUseCase {

    private final IdentifierRepository identifierRepository;
    private final EmployeeIdentifierLookupPort employeeIdentifierLookupPort;

    public GetIdentifierByBusinessKeyService(
            IdentifierRepository identifierRepository,
            EmployeeIdentifierLookupPort employeeIdentifierLookupPort
    ) {
        this.identifierRepository = identifierRepository;
        this.employeeIdentifierLookupPort = employeeIdentifierLookupPort;
    }

    @Override
    public Optional<Identifier> getByBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            String identifierTypeCode
    ) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(ruleSystemCode);
        String normalizedEmployeeTypeCode = normalizeEmployeeTypeCode(employeeTypeCode);
        String normalizedEmployeeNumber = normalizeEmployeeNumber(employeeNumber);
        String normalizedIdentifierTypeCode = normalizeIdentifierTypeCode(identifierTypeCode);

        EmployeeIdentifierContext employee = employeeIdentifierLookupPort
                .findByBusinessKey(normalizedRuleSystemCode, normalizedEmployeeTypeCode, normalizedEmployeeNumber)
                .orElseThrow(() -> new IdentifierEmployeeNotFoundException(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                ));

        return identifierRepository.findByEmployeeIdAndIdentifierTypeCode(
                employee.employeeId(),
                normalizedIdentifierTypeCode
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
