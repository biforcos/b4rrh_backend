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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListEmployeeIdentifiersServiceTest {

    private static final String RULE_SYSTEM_CODE = "ESP";
    private static final String EMPLOYEE_TYPE_CODE = "INTERNAL";
    private static final String EMPLOYEE_NUMBER = "EMP001";

    @Mock
    private IdentifierRepository identifierRepository;
    @Mock
    private EmployeeIdentifierLookupPort employeeIdentifierLookupPort;

    private ListEmployeeIdentifiersService service;

    @BeforeEach
    void setUp() {
        service = new ListEmployeeIdentifiersService(identifierRepository, employeeIdentifierLookupPort);
    }

    @Test
    void listsIdentifiersByEmployeeBusinessKey() {
        when(employeeIdentifierLookupPort.findByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L, EMPLOYEE_NUMBER)));
        when(identifierRepository.findByEmployeeIdOrderByIdentifierTypeCode(10L)).thenReturn(List.of(
                identifier(1L, 10L, "NATIONAL_ID"),
                identifier(2L, 10L, "PASSPORT")
        ));

        List<Identifier> result = service.listByEmployeeBusinessKey(" esp ", " internal ", " EMP001 ");

        assertEquals(2, result.size());
        assertEquals("NATIONAL_ID", result.get(0).getIdentifierTypeCode());
        assertEquals("PASSPORT", result.get(1).getIdentifierTypeCode());
    }

    @Test
    void throwsWhenEmployeeBusinessKeyDoesNotExist() {
        when(employeeIdentifierLookupPort.findByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.empty());

        assertThrows(
                IdentifierEmployeeNotFoundException.class,
                () -> service.listByEmployeeBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER)
        );
    }

    private EmployeeIdentifierContext employeeContext(Long employeeId, String employeeNumber) {
        return new EmployeeIdentifierContext(employeeId, RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, employeeNumber);
    }

    private Identifier identifier(Long id, Long employeeId, String identifierTypeCode) {
        return new Identifier(
                id,
                employeeId,
                identifierTypeCode,
                "VALUE-" + identifierTypeCode,
                null,
                null,
                false,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
