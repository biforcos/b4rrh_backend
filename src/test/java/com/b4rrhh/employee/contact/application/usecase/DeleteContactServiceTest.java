package com.b4rrhh.employee.contact.application.usecase;

import com.b4rrhh.employee.contact.application.port.EmployeeContactContext;
import com.b4rrhh.employee.contact.application.port.EmployeeContactLookupPort;
import com.b4rrhh.employee.contact.domain.exception.ContactEmployeeNotFoundException;
import com.b4rrhh.employee.contact.domain.exception.ContactNotFoundException;
import com.b4rrhh.employee.contact.domain.model.Contact;
import com.b4rrhh.employee.contact.domain.port.ContactRepository;
import com.b4rrhh.rulesystem.domain.model.RuleSystem;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteContactServiceTest {

    private static final String RULE_SYSTEM_CODE = "ESP";
    private static final String EMPLOYEE_TYPE_CODE = "INTERNAL";
    private static final String EMPLOYEE_NUMBER = "EMP001";

    @Mock
    private ContactRepository contactRepository;
    @Mock
    private EmployeeContactLookupPort employeeContactLookupPort;
    @Mock
    private RuleSystemRepository ruleSystemRepository;

    private DeleteContactService service;

    @BeforeEach
    void setUp() {
        service = new DeleteContactService(contactRepository, employeeContactLookupPort, ruleSystemRepository);
    }

    @Test
    void deletesContactByBusinessKey() {
        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeContactLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L, EMPLOYEE_NUMBER)));
        when(contactRepository.findByEmployeeIdAndContactTypeCode(10L, "EMAIL"))
                .thenReturn(Optional.of(existingContact(10L, "EMAIL")));

        service.delete(new DeleteContactCommand(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER, "email"));

        verify(contactRepository).deleteByEmployeeIdAndContactTypeCode(10L, "EMAIL");
    }

    @Test
    void rejectsDeleteWhenContactDoesNotExist() {
        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeContactLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L, EMPLOYEE_NUMBER)));
        when(contactRepository.findByEmployeeIdAndContactTypeCode(10L, "EMAIL")).thenReturn(Optional.empty());

        assertThrows(
                ContactNotFoundException.class,
                () -> service.delete(new DeleteContactCommand(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER, "EMAIL"))
        );
        verify(contactRepository, never()).deleteByEmployeeIdAndContactTypeCode(10L, "EMAIL");
    }

    @Test
    void rejectsDeleteWhenEmployeeBusinessKeyDoesNotExist() {
        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeContactLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.empty());

        assertThrows(
                ContactEmployeeNotFoundException.class,
                () -> service.delete(new DeleteContactCommand(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER, "EMAIL"))
        );
        verify(contactRepository, never()).deleteByEmployeeIdAndContactTypeCode(10L, "EMAIL");
    }

    private EmployeeContactContext employeeContext(Long employeeId, String employeeNumber) {
        return new EmployeeContactContext(employeeId, RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, employeeNumber);
    }

    private Contact existingContact(Long employeeId, String contactTypeCode) {
        return new Contact(
                20L,
                employeeId,
                contactTypeCode,
                "john.doe@example.com",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
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
}