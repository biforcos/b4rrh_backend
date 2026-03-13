package com.b4rrhh.employee.contact.application.usecase;

import com.b4rrhh.employee.contact.application.port.EmployeeContactContext;
import com.b4rrhh.employee.contact.application.port.EmployeeContactLookupPort;
import com.b4rrhh.employee.contact.domain.exception.ContactEmployeeNotFoundException;
import com.b4rrhh.employee.contact.domain.model.Contact;
import com.b4rrhh.employee.contact.domain.port.ContactRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetContactByBusinessKeyServiceTest {

    private static final String RULE_SYSTEM_CODE = "ESP";
    private static final String EMPLOYEE_TYPE_CODE = "INTERNAL";
    private static final String EMPLOYEE_NUMBER = "EMP001";

    @Mock
    private ContactRepository contactRepository;
    @Mock
    private EmployeeContactLookupPort employeeContactLookupPort;

    private GetContactByBusinessKeyService service;

    @BeforeEach
    void setUp() {
        service = new GetContactByBusinessKeyService(contactRepository, employeeContactLookupPort);
    }

    @Test
    void getsContactByBusinessKey() {
        when(employeeContactLookupPort.findByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L, EMPLOYEE_NUMBER)));
        when(contactRepository.findByEmployeeIdAndContactTypeCode(10L, "EMAIL"))
                .thenReturn(Optional.of(existingContact(10L, "EMAIL")));

        Optional<Contact> result = service.getByBusinessKey(" esp ", " internal ", " EMP001 ", "email");

        assertTrue(result.isPresent());
        assertEquals(20L, result.get().getId());
        assertEquals("EMAIL", result.get().getContactTypeCode());
    }

    @Test
    void returnsEmptyWhenContactDoesNotExist() {
        when(employeeContactLookupPort.findByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L, EMPLOYEE_NUMBER)));
        when(contactRepository.findByEmployeeIdAndContactTypeCode(10L, "EMAIL"))
                .thenReturn(Optional.empty());

        Optional<Contact> result = service.getByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER, "EMAIL");

        assertTrue(result.isEmpty());
    }

    @Test
    void throwsWhenEmployeeBusinessKeyDoesNotExist() {
        when(employeeContactLookupPort.findByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.empty());

        assertThrows(
                ContactEmployeeNotFoundException.class,
                () -> service.getByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER, "EMAIL")
        );
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
}