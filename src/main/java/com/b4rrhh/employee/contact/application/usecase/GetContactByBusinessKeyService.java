package com.b4rrhh.employee.contact.application.usecase;

import com.b4rrhh.employee.contact.application.port.EmployeeContactLookupPort;
import com.b4rrhh.employee.contact.application.port.EmployeeContactContext;
import com.b4rrhh.employee.contact.domain.exception.ContactEmployeeNotFoundException;
import com.b4rrhh.employee.contact.domain.model.Contact;
import com.b4rrhh.employee.contact.domain.port.ContactRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetContactByBusinessKeyService implements GetContactByBusinessKeyUseCase {

    private final ContactRepository contactRepository;
    private final EmployeeContactLookupPort employeeContactLookupPort;

    public GetContactByBusinessKeyService(
            ContactRepository contactRepository,
            EmployeeContactLookupPort employeeContactLookupPort
    ) {
        this.contactRepository = contactRepository;
        this.employeeContactLookupPort = employeeContactLookupPort;
    }

    @Override
    public Optional<Contact> getByBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            String contactTypeCode
    ) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(ruleSystemCode);
        String normalizedEmployeeTypeCode = normalizeEmployeeTypeCode(employeeTypeCode);
        String normalizedEmployeeNumber = normalizeEmployeeNumber(employeeNumber);
        String normalizedContactTypeCode = normalizeContactTypeCode(contactTypeCode);

        EmployeeContactContext employee = employeeContactLookupPort
                .findByBusinessKey(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                )
                .orElseThrow(() -> new ContactEmployeeNotFoundException(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                ));

        return contactRepository.findByEmployeeIdAndContactTypeCode(employee.employeeId(), normalizedContactTypeCode);
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

    private String normalizeContactTypeCode(String contactTypeCode) {
        if (contactTypeCode == null || contactTypeCode.trim().isEmpty()) {
            throw new IllegalArgumentException("contactTypeCode is required");
        }

        return contactTypeCode.trim().toUpperCase();
    }
}
