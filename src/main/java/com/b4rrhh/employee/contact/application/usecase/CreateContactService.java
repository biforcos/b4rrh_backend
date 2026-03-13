package com.b4rrhh.employee.contact.application.usecase;

import com.b4rrhh.employee.contact.application.port.EmployeeContactContext;
import com.b4rrhh.employee.contact.application.port.EmployeeContactLookupPort;
import com.b4rrhh.employee.contact.application.service.ContactCatalogValidator;
import com.b4rrhh.employee.contact.domain.exception.ContactAlreadyExistsException;
import com.b4rrhh.employee.contact.domain.exception.ContactEmployeeNotFoundException;
import com.b4rrhh.employee.contact.domain.exception.ContactRuleSystemNotFoundException;
import com.b4rrhh.employee.contact.domain.model.Contact;
import com.b4rrhh.employee.contact.domain.port.ContactRepository;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class CreateContactService implements CreateContactUseCase {

    private final ContactRepository contactRepository;
    private final EmployeeContactLookupPort employeeContactLookupPort;
    private final RuleSystemRepository ruleSystemRepository;
    private final ContactCatalogValidator contactCatalogValidator;

    public CreateContactService(
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
    public Contact create(CreateContactCommand command) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(command.ruleSystemCode());
        String normalizedEmployeeTypeCode = normalizeEmployeeTypeCode(command.employeeTypeCode());
        String normalizedEmployeeNumber = normalizeEmployeeNumber(command.employeeNumber());

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

        String contactTypeCode = contactCatalogValidator.normalizeRequiredCode("contactTypeCode", command.contactTypeCode());
        contactCatalogValidator.validateContactTypeCode(normalizedRuleSystemCode, contactTypeCode, LocalDate.now());

        contactRepository.findByEmployeeIdAndContactTypeCode(employee.employeeId(), contactTypeCode)
                .ifPresent(existing -> {
                    throw new ContactAlreadyExistsException(
                            normalizedRuleSystemCode,
                            normalizedEmployeeTypeCode,
                            normalizedEmployeeNumber,
                            contactTypeCode
                    );
                });

        Contact newContact = new Contact(
                null,
                employee.employeeId(),
                contactTypeCode,
                command.contactValue(),
                null,
                null
        );

        return contactRepository.save(newContact);
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
