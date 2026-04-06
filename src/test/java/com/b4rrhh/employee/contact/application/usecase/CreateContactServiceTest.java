package com.b4rrhh.employee.contact.application.usecase;

import com.b4rrhh.employee.contact.application.port.EmployeeContactContext;
import com.b4rrhh.employee.contact.application.port.EmployeeContactLookupPort;
import com.b4rrhh.employee.contact.application.service.ContactCatalogValidator;
import com.b4rrhh.employee.contact.domain.exception.ContactAlreadyExistsException;
import com.b4rrhh.employee.contact.domain.exception.ContactCatalogValueInvalidException;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateContactServiceTest {

    private static final String RULE_SYSTEM_CODE = "ESP";
    private static final String EMPLOYEE_TYPE_CODE = "INTERNAL";

    @Mock
    private ContactRepository contactRepository;
    @Mock
    private EmployeeContactLookupPort employeeContactLookupPort;
    @Mock
    private RuleSystemRepository ruleSystemRepository;
    @Mock
    private RuleEntityRepository ruleEntityRepository;

    private CreateContactService service;

    @BeforeEach
    void setUp() {
        ContactCatalogValidator contactCatalogValidator = new ContactCatalogValidator(ruleEntityRepository);
        service = new CreateContactService(
                contactRepository,
                employeeContactLookupPort,
                ruleSystemRepository,
                contactCatalogValidator
        );
    }

    @Test
    void createsValidContact() {
        CreateContactCommand command = new CreateContactCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                "EMP001",
                "email",
                "john.doe@example.com"
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeContactLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, "EMP001"))
                .thenReturn(Optional.of(employeeContext(10L, "EMP001")));
        when(ruleEntityRepository.findByBusinessKey(RULE_SYSTEM_CODE, "CONTACT_TYPE", "EMAIL"))
                .thenReturn(Optional.of(activeContactTypeRuleEntity(RULE_SYSTEM_CODE, "EMAIL")));
        when(contactRepository.findByEmployeeIdAndContactTypeCode(10L, "EMAIL")).thenReturn(Optional.empty());
        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation -> {
            Contact input = invocation.getArgument(0);
            return new Contact(
                    99L,
                    input.getEmployeeId(),
                    input.getContactTypeCode(),
                    input.getContactValue(),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );
        });

        Contact created = service.create(command);

        assertEquals(99L, created.getId());
        assertEquals("EMAIL", created.getContactTypeCode());
        assertEquals("john.doe@example.com", created.getContactValue());

        ArgumentCaptor<Contact> captor = ArgumentCaptor.forClass(Contact.class);
        verify(contactRepository).save(captor.capture());
        assertEquals("EMAIL", captor.getValue().getContactTypeCode());
        assertEquals("john.doe@example.com", captor.getValue().getContactValue());
    }

    @Test
    void rejectsCreateWhenContactTypeIsInvalid() {
        CreateContactCommand command = new CreateContactCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                "EMP001",
                "SOCIAL",
                "john.doe@example.com"
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeContactLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, "EMP001"))
                .thenReturn(Optional.of(employeeContext(10L, "EMP001")));
        when(ruleEntityRepository.findByBusinessKey(RULE_SYSTEM_CODE, "CONTACT_TYPE", "SOCIAL"))
                .thenReturn(Optional.empty());

        assertThrows(ContactCatalogValueInvalidException.class, () -> service.create(command));
        verify(contactRepository, never()).save(any(Contact.class));
    }

    @Test
    void rejectsCreateWhenContactValueIsInvalidForType() {
        CreateContactCommand command = new CreateContactCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                "EMP001",
                "EMAIL",
                "not-an-email"
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeContactLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, "EMP001"))
                .thenReturn(Optional.of(employeeContext(10L, "EMP001")));
        when(ruleEntityRepository.findByBusinessKey(RULE_SYSTEM_CODE, "CONTACT_TYPE", "EMAIL"))
                .thenReturn(Optional.of(activeContactTypeRuleEntity(RULE_SYSTEM_CODE, "EMAIL")));
        when(contactRepository.findByEmployeeIdAndContactTypeCode(10L, "EMAIL")).thenReturn(Optional.empty());

        assertThrows(ContactValueInvalidException.class, () -> service.create(command));
        verify(contactRepository, never()).save(any(Contact.class));
    }

    @Test
    void rejectsCreateWhenDuplicateContactTypeExistsForEmployee() {
        CreateContactCommand command = new CreateContactCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                "EMP001",
                "EMAIL",
                "john.doe@example.com"
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeContactLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, "EMP001"))
                .thenReturn(Optional.of(employeeContext(10L, "EMP001")));
        when(ruleEntityRepository.findByBusinessKey(RULE_SYSTEM_CODE, "CONTACT_TYPE", "EMAIL"))
                .thenReturn(Optional.of(activeContactTypeRuleEntity(RULE_SYSTEM_CODE, "EMAIL")));
        when(contactRepository.findByEmployeeIdAndContactTypeCode(10L, "EMAIL"))
                .thenReturn(Optional.of(existingEmailContact(10L)));

        assertThrows(ContactAlreadyExistsException.class, () -> service.create(command));
        verify(contactRepository, never()).save(any(Contact.class));
    }

    @Test
    void allowsSameContactTypeForDifferentEmployees() {
        CreateContactCommand command = new CreateContactCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                "EMP002",
                "EMAIL",
                "second@example.com"
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeContactLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, "EMP002"))
                .thenReturn(Optional.of(employeeContext(11L, "EMP002")));
        when(ruleEntityRepository.findByBusinessKey(RULE_SYSTEM_CODE, "CONTACT_TYPE", "EMAIL"))
                .thenReturn(Optional.of(activeContactTypeRuleEntity(RULE_SYSTEM_CODE, "EMAIL")));
        when(contactRepository.findByEmployeeIdAndContactTypeCode(11L, "EMAIL"))
                .thenReturn(Optional.empty());
        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation -> {
            Contact input = invocation.getArgument(0);
            return new Contact(
                    101L,
                    input.getEmployeeId(),
                    input.getContactTypeCode(),
                    input.getContactValue(),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );
        });

        Contact created = service.create(command);

        assertEquals(101L, created.getId());
        assertEquals(11L, created.getEmployeeId());
        assertEquals("EMAIL", created.getContactTypeCode());
        verify(contactRepository).findByEmployeeIdAndContactTypeCode(11L, "EMAIL");
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

    private Contact existingEmailContact(Long employeeId) {
        return new Contact(
                20L,
                employeeId,
                "EMAIL",
                "existing@example.com",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}