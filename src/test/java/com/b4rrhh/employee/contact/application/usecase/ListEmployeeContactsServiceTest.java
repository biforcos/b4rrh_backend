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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListEmployeeContactsServiceTest {

    private static final String RULE_SYSTEM_CODE = "ESP";
    private static final String EMPLOYEE_TYPE_CODE = "INTERNAL";
    private static final String EMPLOYEE_NUMBER = "EMP001";

    @Mock
    private ContactRepository contactRepository;
    @Mock
    private EmployeeContactLookupPort employeeContactLookupPort;

    private ListEmployeeContactsService service;

    @BeforeEach
    void setUp() {
        service = new ListEmployeeContactsService(contactRepository, employeeContactLookupPort);
    }

    @Test
    void listsContactsByEmployeeBusinessKey() {
        when(employeeContactLookupPort.findByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L, EMPLOYEE_NUMBER)));
        when(contactRepository.findByEmployeeIdOrderByContactTypeCode(10L)).thenReturn(List.of(
                new Contact(1L, 10L, "EMAIL", "john.doe@example.com", LocalDateTime.now(), LocalDateTime.now()),
                new Contact(2L, 10L, "MOBILE", "+34 600 111 222", LocalDateTime.now(), LocalDateTime.now())
        ));

        List<Contact> result = service.listByEmployeeBusinessKey(" esp ", " internal ", " EMP001 ");

        assertEquals(2, result.size());
        assertEquals("EMAIL", result.get(0).getContactTypeCode());
        assertEquals("MOBILE", result.get(1).getContactTypeCode());
    }

    @Test
    void throwsWhenEmployeeBusinessKeyDoesNotExist() {
        when(employeeContactLookupPort.findByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.empty());

        assertThrows(
                ContactEmployeeNotFoundException.class,
                () -> service.listByEmployeeBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER)
        );
    }

    private EmployeeContactContext employeeContext(Long employeeId, String employeeNumber) {
        return new EmployeeContactContext(employeeId, RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, employeeNumber);
    }
}