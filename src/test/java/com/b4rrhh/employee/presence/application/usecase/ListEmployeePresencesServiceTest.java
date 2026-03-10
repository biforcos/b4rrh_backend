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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListEmployeePresencesServiceTest {

    @Mock
    private PresenceRepository presenceRepository;
    @Mock
    private EmployeePresenceLookupPort employeePresenceLookupPort;

    private ListEmployeePresencesService service;

    @BeforeEach
    void setUp() {
        service = new ListEmployeePresencesService(presenceRepository, employeePresenceLookupPort);
    }

    @Test
    void listsEmployeePresenceHistory() {
        when(employeePresenceLookupPort.findById(10L)).thenReturn(Optional.of(new EmployeePresenceContext(10L, "ESP")));

        List<Presence> expected = List.of(
                presence(1L, 10L, 1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15)),
                presence(2L, 10L, 2, LocalDate.of(2026, 1, 16), null)
        );
        when(presenceRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(expected);

        List<Presence> result = service.listByEmployeeId(10L);

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getPresenceNumber());
        assertEquals(LocalDate.of(2026, 1, 1), result.get(0).getStartDate());
        assertEquals(2, result.get(1).getPresenceNumber());
        assertEquals(LocalDate.of(2026, 1, 16), result.get(1).getStartDate());
    }

    @Test
    void rejectsListWhenEmployeeDoesNotExist() {
        when(employeePresenceLookupPort.findById(10L)).thenReturn(Optional.empty());

        assertThrows(PresenceEmployeeNotFoundException.class, () -> service.listByEmployeeId(10L));
    }

    private Presence presence(Long id, Long employeeId, Integer number, LocalDate startDate, LocalDate endDate) {
        return new Presence(
                id,
                employeeId,
                number,
                "AC01",
                "ENT01",
                null,
                startDate,
                endDate,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
