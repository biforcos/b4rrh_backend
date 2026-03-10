package com.b4rrhh.employee.presence.application.usecase;

import com.b4rrhh.employee.presence.application.port.EmployeePresenceContext;
import com.b4rrhh.employee.presence.application.port.EmployeePresenceLookupPort;
import com.b4rrhh.employee.presence.application.service.PresenceCatalogValidator;
import com.b4rrhh.employee.presence.domain.exception.ActivePresenceAlreadyExistsException;
import com.b4rrhh.employee.presence.domain.exception.InvalidPresenceDateRangeException;
import com.b4rrhh.employee.presence.domain.exception.PresenceCatalogValueInvalidException;
import com.b4rrhh.employee.presence.domain.exception.PresenceOverlapException;
import com.b4rrhh.employee.presence.domain.model.Presence;
import com.b4rrhh.employee.presence.domain.port.PresenceRepository;
import com.b4rrhh.rulesystem.domain.model.RuleSystem;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatePresenceServiceTest {

    @Mock
    private PresenceRepository presenceRepository;
    @Mock
    private EmployeePresenceLookupPort employeePresenceLookupPort;
    @Mock
    private RuleSystemRepository ruleSystemRepository;
    private PresenceCatalogValidator presenceCatalogValidator;

    private CreatePresenceService service;

    @BeforeEach
    void setUp() {
        presenceCatalogValidator = new TestPresenceCatalogValidator();
        service = new CreatePresenceService(
                presenceRepository,
                employeePresenceLookupPort,
                ruleSystemRepository,
                presenceCatalogValidator
        );
    }

    @Test
    void createsPresenceAndAssignsNextPresenceNumber() {
        CreatePresenceCommand command = new CreatePresenceCommand(
                10L,
                "ac01",
                "ent01",
                null,
                LocalDate.of(2026, 1, 10),
                null
        );

        when(employeePresenceLookupPort.findByIdForUpdate(10L)).thenReturn(Optional.of(new EmployeePresenceContext(10L, "ESP")));
        when(ruleSystemRepository.findByCode("ESP")).thenReturn(Optional.of(ruleSystem("ESP")));
        when(presenceRepository.existsActivePresence(10L)).thenReturn(false);
        when(presenceRepository.existsOverlappingPeriod(10L, LocalDate.of(2026, 1, 10), null)).thenReturn(false);
        when(presenceRepository.findMaxPresenceNumberByEmployeeId(10L)).thenReturn(Optional.of(4));
        when(presenceRepository.save(any(Presence.class))).thenAnswer(invocation -> {
            Presence input = invocation.getArgument(0);
            return new Presence(
                    99L,
                    input.getEmployeeId(),
                    input.getPresenceNumber(),
                    input.getCompanyCode(),
                    input.getEntryReasonCode(),
                    input.getExitReasonCode(),
                    input.getStartDate(),
                    input.getEndDate(),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );
        });

        Presence created = service.create(command);

        assertEquals(99L, created.getId());
        assertEquals(5, created.getPresenceNumber());
        assertEquals("AC01", created.getCompanyCode());
        assertEquals("ENT01", created.getEntryReasonCode());

        ArgumentCaptor<Presence> captor = ArgumentCaptor.forClass(Presence.class);
        verify(presenceRepository).save(captor.capture());
        assertEquals(5, captor.getValue().getPresenceNumber());

        InOrder inOrder = inOrder(employeePresenceLookupPort, presenceRepository);
        inOrder.verify(employeePresenceLookupPort).findByIdForUpdate(10L);
        inOrder.verify(presenceRepository).existsActivePresence(10L);
        inOrder.verify(presenceRepository).existsOverlappingPeriod(10L, LocalDate.of(2026, 1, 10), null);
        inOrder.verify(presenceRepository).findMaxPresenceNumberByEmployeeId(10L);
    }

    @Test
    void rejectsInvalidCatalogValue() {
        CreatePresenceCommand command = new CreatePresenceCommand(
                10L,
                "bad",
                "ENT01",
                null,
                LocalDate.of(2026, 1, 10),
                null
        );

        when(employeePresenceLookupPort.findByIdForUpdate(10L)).thenReturn(Optional.of(new EmployeePresenceContext(10L, "ESP")));
        when(ruleSystemRepository.findByCode("ESP")).thenReturn(Optional.of(ruleSystem("ESP")));

        assertThrows(PresenceCatalogValueInvalidException.class, () -> service.create(command));
        verify(presenceRepository, never()).save(any(Presence.class));
    }

    @Test
    void rejectsInvalidDateRange() {
        CreatePresenceCommand command = new CreatePresenceCommand(
                10L,
                "AC01",
                "ENT01",
                null,
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 1, 10)
        );

        when(employeePresenceLookupPort.findByIdForUpdate(10L)).thenReturn(Optional.of(new EmployeePresenceContext(10L, "ESP")));
        when(ruleSystemRepository.findByCode("ESP")).thenReturn(Optional.of(ruleSystem("ESP")));
        when(presenceRepository.existsOverlappingPeriod(10L, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 10))).thenReturn(false);
        when(presenceRepository.findMaxPresenceNumberByEmployeeId(10L)).thenReturn(Optional.empty());

        assertThrows(InvalidPresenceDateRangeException.class, () -> service.create(command));
        verify(presenceRepository, never()).save(any(Presence.class));
    }

    @Test
    void rejectsOverlappingPresence() {
        CreatePresenceCommand command = new CreatePresenceCommand(
                10L,
                "AC01",
                "ENT01",
                null,
                LocalDate.of(2026, 1, 10),
                null
        );

        when(employeePresenceLookupPort.findByIdForUpdate(10L)).thenReturn(Optional.of(new EmployeePresenceContext(10L, "ESP")));
        when(ruleSystemRepository.findByCode("ESP")).thenReturn(Optional.of(ruleSystem("ESP")));
        when(presenceRepository.existsActivePresence(10L)).thenReturn(false);
        when(presenceRepository.existsOverlappingPeriod(10L, LocalDate.of(2026, 1, 10), null)).thenReturn(true);

        assertThrows(PresenceOverlapException.class, () -> service.create(command));
    }

    @Test
    void rejectsSecondActivePresence() {
        CreatePresenceCommand command = new CreatePresenceCommand(
                10L,
                "AC01",
                "ENT01",
                null,
                LocalDate.of(2026, 1, 10),
                null
        );

        when(employeePresenceLookupPort.findByIdForUpdate(10L)).thenReturn(Optional.of(new EmployeePresenceContext(10L, "ESP")));
        when(ruleSystemRepository.findByCode("ESP")).thenReturn(Optional.of(ruleSystem("ESP")));
        when(presenceRepository.existsActivePresence(10L)).thenReturn(true);

        assertThrows(ActivePresenceAlreadyExistsException.class, () -> service.create(command));
        verify(presenceRepository, never()).save(any(Presence.class));
    }

    private static final class TestPresenceCatalogValidator extends PresenceCatalogValidator {

        private TestPresenceCatalogValidator() {
            super(null);
        }

        @Override
        public void validateCompanyCode(String ruleSystemCode, String companyCode, LocalDate referenceDate) {
            if ("BAD".equals(companyCode)) {
                throw new PresenceCatalogValueInvalidException("companyCode", companyCode);
            }
        }

        @Override
        public void validateEntryReasonCode(String ruleSystemCode, String entryReasonCode, LocalDate referenceDate) {
            // No-op: specific entry-reason validation is out of scope for this use case unit test.
        }

        @Override
        public void validateExitReasonCode(String ruleSystemCode, String exitReasonCode, LocalDate referenceDate) {
            // No-op: specific exit-reason validation is out of scope for this use case unit test.
        }

        @Override
        public String normalizeRequiredCode(String fieldName, String value) {
            if (value == null || value.trim().isEmpty()) {
                throw new PresenceCatalogValueInvalidException(fieldName, String.valueOf(value));
            }

            return value.trim().toUpperCase();
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
}
