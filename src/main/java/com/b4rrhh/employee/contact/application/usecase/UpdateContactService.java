package com.b4rrhh.employee.contact.application.usecase;

import com.b4rrhh.employee.contact.application.port.EmployeeContactContext;
import com.b4rrhh.employee.contact.application.port.EmployeeContactLookupPort;
import com.b4rrhh.employee.contact.application.service.ContactCatalogValidator;
import com.b4rrhh.employee.contact.domain.exception.ContactEmployeeNotFoundException;
import com.b4rrhh.employee.contact.domain.exception.ContactNotFoundException;
import com.b4rrhh.employee.contact.domain.exception.ContactRuleSystemNotFoundException;
import com.b4rrhh.employee.contact.domain.exception.ContactTypeMutationNotAllowedException;
import com.b4rrhh.employee.contact.domain.model.Contact;
import com.b4rrhh.employee.contact.domain.port.ContactRepository;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class UpdateContactService implements UpdateContactUseCase {

    private final ContactRepository contactRepository;
    private final EmployeeContactLookupPort employeeContactLookupPort;
    private final RuleSystemRepository ruleSystemRepository;
    private final ContactCatalogValidator contactCatalogValidator;

    public UpdateContactService(
            ContactRepository contactRepository,
            EmployeeContactLookupPort employeeContactLookupPort,
            RuleSystemRepository ruleSystemRepository,
            ContactCatalogValidator contactCatalogValidator
    ) {
        this.contactRepository = contactRepository;
        this.employeeContactLookupPort = employeeContactLookupPort;
        this.ruleSystemRepository = ruleSystemRepository;
        this.contactCatalogValidator = contactCatalogValidator;
    }

    @Override
    @Transactional
    public Contact update(UpdateContactCommand command) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(command.ruleSystemCode());
        String normalizedEmployeeTypeCode = normalizeEmployeeTypeCode(command.employeeTypeCode());
        String normalizedEmployeeNumber = normalizeEmployeeNumber(command.employeeNumber());

        String contactTypeCode = contactCatalogValidator.normalizeRequiredCode("contactTypeCode", command.contactTypeCode());

        ruleSystemRepository.findByCode(normalizedRuleSystemCode)
                .orElseThrow(() -> new ContactRuleSystemNotFoundException(normalizedRuleSystemCode));

        EmployeeContactContext employee = employeeContactLookupPort
                .findByBusinessKeyForUpdate(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                )
                .orElseThrow(() -> new ContactEmployeeNotFoundException(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                ));

        Contact existing = contactRepository
                .findByEmployeeIdAndContactTypeCode(employee.employeeId(), contactTypeCode)
                .orElseThrow(() -> new ContactNotFoundException(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber,
                        contactTypeCode
                ));

        contactCatalogValidator.validateContactTypeCode(normalizedRuleSystemCode, contactTypeCode, LocalDate.now());

        if (!existing.getContactTypeCode().equals(contactTypeCode)) {
            throw new ContactTypeMutationNotAllowedException(contactTypeCode);
        }

        Contact updated = existing.updateContactValue(command.contactValue());
        return contactRepository.save(updated);
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
