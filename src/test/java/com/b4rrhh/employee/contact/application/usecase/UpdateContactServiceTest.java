package com.b4rrhh.employee.contact.application.usecase;

import com.b4rrhh.employee.contact.application.port.EmployeeContactContext;
import com.b4rrhh.employee.contact.application.port.EmployeeContactLookupPort;
import com.b4rrhh.employee.contact.application.service.ContactCatalogValidator;
import com.b4rrhh.employee.contact.domain.exception.ContactEmployeeNotFoundException;
import com.b4rrhh.employee.contact.domain.exception.ContactNotFoundException;
import com.b4rrhh.employee.contact.domain.exception.ContactValueInvalidException;
import com.b4rrhh.employee.contact.domain.model.Contact;
import com.b4rrhh.employee.contact.domain.port.ContactRepository;
import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.model.RuleSystem;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateContactServiceTest {

    private static final String RULE_SYSTEM_CODE = "ESP";
    private static final String EMPLOYEE_TYPE_CODE = "INTERNAL";
    private static final String EMPLOYEE_NUMBER = "EMP001";

    @Mock
    private ContactRepository contactRepository;
    @Mock
    private EmployeeContactLookupPort employeeContactLookupPort;
    @Mock
    private RuleSystemRepository ruleSystemRepository;
    @Mock
    private RuleEntityRepository ruleEntityRepository;

    private UpdateContactService service;

    @BeforeEach
    void setUp() {
        ContactCatalogValidator contactCatalogValidator = new ContactCatalogValidator(ruleEntityRepository);
        service = new UpdateContactService(
                contactRepository,
                employeeContactLookupPort,
                ruleSystemRepository,
                contactCatalogValidator
        );
    }

    @Test
    void updatesContactValueByBusinessKey() {
        UpdateContactCommand command = new UpdateContactCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                "email",
                "new.email@example.com"
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeContactLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L, EMPLOYEE_NUMBER)));
        when(contactRepository.findByEmployeeIdAndContactTypeCode(10L, "EMAIL"))
                .thenReturn(Optional.of(existingEmailContact()));
        when(ruleEntityRepository.findByBusinessKey(RULE_SYSTEM_CODE, "CONTACT_TYPE", "EMAIL"))
                .thenReturn(Optional.of(activeContactTypeRuleEntity(RULE_SYSTEM_CODE, "EMAIL")));
        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Contact updated = service.update(command);

        assertEquals(20L, updated.getId());
        assertEquals("EMAIL", updated.getContactTypeCode());
        assertEquals("new.email@example.com", updated.getContactValue());
    }

    @Test
    void rejectsUpdateWhenContactDoesNotExist() {
        UpdateContactCommand command = new UpdateContactCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                "EMAIL",
                "john.doe@example.com"
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeContactLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L, EMPLOYEE_NUMBER)));
        when(contactRepository.findByEmployeeIdAndContactTypeCode(10L, "EMAIL")).thenReturn(Optional.empty());

        assertThrows(ContactNotFoundException.class, () -> service.update(command));
    }

    @Test
    void rejectsUpdateWhenContactValueIsInvalid() {
        UpdateContactCommand command = new UpdateContactCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                "EMAIL",
                "invalid-email"
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeContactLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L, EMPLOYEE_NUMBER)));
        when(contactRepository.findByEmployeeIdAndContactTypeCode(10L, "EMAIL"))
                .thenReturn(Optional.of(existingEmailContact()));
        when(ruleEntityRepository.findByBusinessKey(RULE_SYSTEM_CODE, "CONTACT_TYPE", "EMAIL"))
                .thenReturn(Optional.of(activeContactTypeRuleEntity(RULE_SYSTEM_CODE, "EMAIL")));

        assertThrows(ContactValueInvalidException.class, () -> service.update(command));
    }

    @Test
    void rejectsUpdateWhenEmployeeBusinessKeyDoesNotExist() {
        UpdateContactCommand command = new UpdateContactCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                "EMAIL",
                "john.doe@example.com"
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeContactLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.empty());

        assertThrows(ContactEmployeeNotFoundException.class, () -> service.update(command));
    }

    private EmployeeContactContext employeeContext(Long employeeId, String employeeNumber) {
        return new EmployeeContactContext(employeeId, RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, employeeNumber);
    }

    private RuleSystem ruleSystem(String code) {
        return new RuleSystem(
                1L,
                code,
                "Spain",
                "ESP",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private RuleEntity activeContactTypeRuleEntity(String ruleSystemCode, String code) {
        return new RuleEntity(
                1L,
                ruleSystemCode,
                "CONTACT_TYPE",
                code,
                code,
                null,
                true,
                LocalDate.of(1900, 1, 1),
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private Contact existingEmailContact() {
        return new Contact(
                20L,
                10L,
                "EMAIL",
                "existing@example.com",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}