package com.b4rrhh.employee.presence.application.usecase;

import com.b4rrhh.employee.presence.application.port.EmployeePresenceContext;
import com.b4rrhh.employee.presence.application.port.EmployeePresenceLookupPort;
import com.b4rrhh.employee.presence.application.service.PresenceCatalogValidator;
import com.b4rrhh.employee.presence.domain.exception.PresenceAlreadyClosedException;
import com.b4rrhh.employee.presence.domain.exception.PresenceNotFoundException;
import com.b4rrhh.employee.presence.domain.model.Presence;
import com.b4rrhh.employee.presence.domain.port.PresenceRepository;
import com.b4rrhh.rulesystem.domain.model.RuleSystem;
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
class ClosePresenceServiceTest {

    private static final String RULE_SYSTEM_CODE = "ESP";
    private static final String EMPLOYEE_TYPE_CODE = "INTERNAL";
    private static final String EMPLOYEE_NUMBER = "EMP001";

    @Mock
    private PresenceRepository presenceRepository;
    @Mock
    private EmployeePresenceLookupPort employeePresenceLookupPort;
    @Mock
    private RuleSystemRepository ruleSystemRepository;
    private PresenceCatalogValidator presenceCatalogValidator;

    private ClosePresenceService service;

    @BeforeEach
    void setUp() {
        presenceCatalogValidator = new TestPresenceCatalogValidator();
        service = new ClosePresenceService(
                presenceRepository,
                employeePresenceLookupPort,
                ruleSystemRepository,
                presenceCatalogValidator
        );
    }

    @Test
    void closesActivePresence() {
        ClosePresenceCommand command = new ClosePresenceCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                1,
                LocalDate.of(2026, 2, 1),
                "ext01"
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeePresenceLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(presenceRepository.findByEmployeeIdAndPresenceNumber(10L, 1)).thenReturn(Optional.of(activePresence()));
        when(presenceRepository.save(any(Presence.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Presence closed = service.close(command);

        assertEquals(LocalDate.of(2026, 2, 1), closed.getEndDate());
        assertEquals("EXT01", closed.getExitReasonCode());
    }

    @Test
    void rejectsCloseAlreadyClosedPresence() {
        ClosePresenceCommand command = new ClosePresenceCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                1,
                LocalDate.of(2026, 2, 1),
                null
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeePresenceLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(presenceRepository.findByEmployeeIdAndPresenceNumber(10L, 1)).thenReturn(Optional.of(closedPresence()));

        assertThrows(PresenceAlreadyClosedException.class, () -> service.close(command));
    }

    @Test
    void rejectsCloseWhenPresenceDoesNotExist() {
        ClosePresenceCommand command = new ClosePresenceCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                9,
                LocalDate.of(2026, 2, 1),
                null
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeePresenceLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(presenceRepository.findByEmployeeIdAndPresenceNumber(10L, 9)).thenReturn(Optional.empty());

        assertThrows(PresenceNotFoundException.class, () -> service.close(command));
    }

    private EmployeePresenceContext employeeContext(Long employeeId) {
        return new EmployeePresenceContext(employeeId, RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER);
    }

    private static final class TestPresenceCatalogValidator extends PresenceCatalogValidator {

        private TestPresenceCatalogValidator() {
            super(null);
        }

        @Override
        public void validateExitReasonCode(String ruleSystemCode, String exitReasonCode, LocalDate referenceDate) {
            // No-op: catalog constraints are validated in PresenceCatalogValidator tests.
        }

        @Override
        public String normalizeOptionalCode(String value) {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }

            return value.trim().toUpperCase();
        }
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

    private Presence activePresence() {
        return new Presence(
                20L,
                10L,
                1,
                "AC01",
                "ENT01",
                null,
                LocalDate.of(2026, 1, 1),
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private Presence closedPresence() {
        return new Presence(
                20L,
                10L,
                1,
                "AC01",
                "ENT01",
                "EXT01",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 15),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
