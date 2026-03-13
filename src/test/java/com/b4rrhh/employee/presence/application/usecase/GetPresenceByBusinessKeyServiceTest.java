package com.b4rrhh.employee.presence.application.usecase;

import com.b4rrhh.employee.presence.application.port.EmployeePresenceContext;
import com.b4rrhh.employee.presence.application.port.EmployeePresenceLookupPort;
import com.b4rrhh.employee.presence.domain.exception.PresenceEmployeeNotFoundException;
import com.b4rrhh.employee.presence.domain.model.Presence;
import com.b4rrhh.employee.presence.domain.port.PresenceRepository;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPresenceByBusinessKeyServiceTest {

    private static final String RULE_SYSTEM_CODE = "ESP";
    private static final String EMPLOYEE_TYPE_CODE = "INTERNAL";
    private static final String EMPLOYEE_NUMBER = "EMP001";

    @Mock
    private PresenceRepository presenceRepository;
    @Mock
    private EmployeePresenceLookupPort employeePresenceLookupPort;

    private GetPresenceByBusinessKeyService service;

    @BeforeEach
    void setUp() {
        service = new GetPresenceByBusinessKeyService(presenceRepository, employeePresenceLookupPort);
    }

    @Test
    void getsPresenceByBusinessKeyAndPresenceNumber() {
        when(employeePresenceLookupPort.findByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(presenceRepository.findByEmployeeIdAndPresenceNumber(10L, 1))
                .thenReturn(Optional.of(presence(20L, 10L, 1)));

        Optional<Presence> result = service.getByBusinessKey(" esp ", " internal ", " EMP001 ", 1);

        assertTrue(result.isPresent());
        assertEquals(20L, result.get().getId());
        assertEquals(1, result.get().getPresenceNumber());
    }

    @Test
    void returnsEmptyWhenPresenceNumberDoesNotExist() {
        when(employeePresenceLookupPort.findByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(presenceRepository.findByEmployeeIdAndPresenceNumber(10L, 9)).thenReturn(Optional.empty());

        Optional<Presence> result = service.getByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER, 9);

        assertTrue(result.isEmpty());
    }

    @Test
    void throwsWhenEmployeeBusinessKeyDoesNotExist() {
        when(employeePresenceLookupPort.findByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.empty());

        assertThrows(
                PresenceEmployeeNotFoundException.class,
                () -> service.getByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER, 1)
        );
    }

    private EmployeePresenceContext employeeContext(Long employeeId) {
        return new EmployeePresenceContext(employeeId, RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER);
    }

    private Presence presence(Long id, Long employeeId, Integer presenceNumber) {
        return new Presence(
                id,
                employeeId,
                presenceNumber,
                "AC01",
                "ENT01",
                null,
                LocalDate.of(2026, 1, 1),
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
