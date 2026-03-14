package com.b4rrhh.employee.identifier.application.usecase;

import com.b4rrhh.employee.identifier.application.port.EmployeeIdentifierContext;
import com.b4rrhh.employee.identifier.application.port.EmployeeIdentifierLookupPort;
import com.b4rrhh.employee.identifier.domain.exception.IdentifierEmployeeNotFoundException;
import com.b4rrhh.employee.identifier.domain.model.Identifier;
import com.b4rrhh.employee.identifier.domain.port.IdentifierRepository;
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
class GetIdentifierByBusinessKeyServiceTest {

    private static final String RULE_SYSTEM_CODE = "ESP";
    private static final String EMPLOYEE_TYPE_CODE = "INTERNAL";
    private static final String EMPLOYEE_NUMBER = "EMP001";

    @Mock
    private IdentifierRepository identifierRepository;
    @Mock
    private EmployeeIdentifierLookupPort employeeIdentifierLookupPort;

    private GetIdentifierByBusinessKeyService service;

    @BeforeEach
    void setUp() {
        service = new GetIdentifierByBusinessKeyService(identifierRepository, employeeIdentifierLookupPort);
    }

    @Test
    void getsIdentifierByBusinessKey() {
        when(employeeIdentifierLookupPort.findByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L, EMPLOYEE_NUMBER)));
        when(identifierRepository.findByEmployeeIdAndIdentifierTypeCode(10L, "NATIONAL_ID"))
                .thenReturn(Optional.of(existingIdentifier(10L, "NATIONAL_ID")));

        Optional<Identifier> result = service.getByBusinessKey(" esp ", " internal ", " EMP001 ", "national_id");

        assertTrue(result.isPresent());
        assertEquals(20L, result.get().getId());
        assertEquals("NATIONAL_ID", result.get().getIdentifierTypeCode());
    }

    @Test
    void returnsEmptyWhenIdentifierDoesNotExist() {
        when(employeeIdentifierLookupPort.findByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L, EMPLOYEE_NUMBER)));
        when(identifierRepository.findByEmployeeIdAndIdentifierTypeCode(10L, "NATIONAL_ID"))
                .thenReturn(Optional.empty());

        Optional<Identifier> result = service.getByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER, "NATIONAL_ID");

        assertTrue(result.isEmpty());
    }

    @Test
    void throwsWhenEmployeeBusinessKeyDoesNotExist() {
        when(employeeIdentifierLookupPort.findByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.empty());

        assertThrows(
                IdentifierEmployeeNotFoundException.class,
                () -> service.getByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER, "NATIONAL_ID")
        );
    }

    private EmployeeIdentifierContext employeeContext(Long employeeId, String employeeNumber) {
        return new EmployeeIdentifierContext(employeeId, RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, employeeNumber);
    }

    private Identifier existingIdentifier(Long employeeId, String identifierTypeCode) {
        return new Identifier(
                20L,
                employeeId,
                identifierTypeCode,
                "12345678A",
                null,
                null,
                false,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
