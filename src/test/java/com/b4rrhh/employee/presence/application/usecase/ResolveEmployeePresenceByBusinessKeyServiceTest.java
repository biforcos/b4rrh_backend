package com.b4rrhh.employee.presence.application.usecase;

import com.b4rrhh.employee.presence.application.port.EmployeePresenceContext;
import com.b4rrhh.employee.presence.application.port.EmployeePresenceLookupPort;
import com.b4rrhh.employee.presence.domain.exception.PresenceEmployeeBusinessKeyMismatchException;
import com.b4rrhh.employee.presence.domain.exception.PresenceEmployeeNotFoundException;
import com.b4rrhh.employee.presence.domain.exception.PresenceRuleSystemNotFoundException;
import com.b4rrhh.rulesystem.domain.model.RuleSystem;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResolveEmployeePresenceByBusinessKeyServiceTest {

    @Mock
    private EmployeePresenceLookupPort employeePresenceLookupPort;
    @Mock
    private RuleSystemRepository ruleSystemRepository;

    private ResolveEmployeePresenceByBusinessKeyService service;

    @BeforeEach
    void setUp() {
        service = new ResolveEmployeePresenceByBusinessKeyService(employeePresenceLookupPort, ruleSystemRepository);
    }

    @Test
    void resolvesEmployeeContextByBusinessKey() {
        when(ruleSystemRepository.findByCode("ESP")).thenReturn(Optional.of(ruleSystem("ESP")));
        when(employeePresenceLookupPort.findByBusinessKey("ESP", "EMP001"))
                .thenReturn(Optional.of(new EmployeePresenceContext(10L, "ESP")));

        EmployeePresenceContext result = service.resolve(" esp ", " EMP001 ");

        assertEquals(10L, result.employeeId());
        assertEquals("ESP", result.ruleSystemCode());
    }

    @Test
    void throwsWhenRuleSystemDoesNotExist() {
        when(ruleSystemRepository.findByCode("ESP")).thenReturn(Optional.empty());

        assertThrows(
                PresenceRuleSystemNotFoundException.class,
                () -> service.resolve("ESP", "EMP001")
        );
    }

    @Test
    void throwsWhenEmployeeDoesNotExist() {
        when(ruleSystemRepository.findByCode("ESP")).thenReturn(Optional.of(ruleSystem("ESP")));
        when(employeePresenceLookupPort.findByBusinessKey("ESP", "EMP001")).thenReturn(Optional.empty());

        assertThrows(
                PresenceEmployeeNotFoundException.class,
                () -> service.resolve("ESP", "EMP001")
        );
    }

    @Test
    void throwsWhenBusinessKeyIsInconsistentWithStoredData() {
        when(ruleSystemRepository.findByCode("ESP")).thenReturn(Optional.of(ruleSystem("ESP")));
        when(employeePresenceLookupPort.findByBusinessKey("ESP", "EMP001"))
                .thenReturn(Optional.of(new EmployeePresenceContext(10L, "FRA")));

        assertThrows(
                PresenceEmployeeBusinessKeyMismatchException.class,
                () -> service.resolve("ESP", "EMP001")
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
