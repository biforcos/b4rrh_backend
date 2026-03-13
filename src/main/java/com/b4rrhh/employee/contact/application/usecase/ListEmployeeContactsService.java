package com.b4rrhh.employee.contact.application.usecase;

import com.b4rrhh.employee.contact.application.port.EmployeeContactLookupPort;
import com.b4rrhh.employee.contact.application.port.EmployeeContactContext;
import com.b4rrhh.employee.contact.domain.exception.ContactEmployeeNotFoundException;
import com.b4rrhh.employee.contact.domain.model.Contact;
import com.b4rrhh.employee.contact.domain.port.ContactRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListEmployeeContactsService implements ListEmployeeContactsUseCase {

    private final ContactRepository contactRepository;
    private final EmployeeContactLookupPort employeeContactLookupPort;

    public ListEmployeeContactsService(ContactRepository contactRepository, EmployeeContactLookupPort employeeContactLookupPort) {
        this.contactRepository = contactRepository;
        this.employeeContactLookupPort = employeeContactLookupPort;
    }

    @Override
    public List<Contact> listByEmployeeBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(ruleSystemCode);
        String normalizedEmployeeTypeCode = normalizeEmployeeTypeCode(employeeTypeCode);
        String normalizedEmployeeNumber = normalizeEmployeeNumber(employeeNumber);

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

        return contactRepository.findByEmployeeIdOrderByContactTypeCode(employee.employeeId());
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
