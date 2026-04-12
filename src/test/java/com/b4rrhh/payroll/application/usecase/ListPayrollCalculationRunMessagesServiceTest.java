package com.b4rrhh.payroll.application.usecase;

import com.b4rrhh.payroll.domain.exception.InvalidPayrollArgumentException;
import com.b4rrhh.payroll.domain.model.CalculationRunMessage;
import com.b4rrhh.payroll.domain.port.CalculationRunMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListPayrollCalculationRunMessagesServiceTest {

    @Mock
    private CalculationRunMessageRepository calculationRunMessageRepository;

    @InjectMocks
    private ListPayrollCalculationRunMessagesService service;

    @Test
    void listsMessagesFromRepositoryWithoutExtraFiltering() {
        LocalDateTime now = LocalDateTime.of(2026, 4, 11, 10, 0);
        when(calculationRunMessageRepository.findByRunIdOrderByCreatedAtAscIdAsc(7L)).thenReturn(List.of(
                new CalculationRunMessage(1L, 7L, "A", "INFO", "ok", "{}", "ESP", "INTERNAL", "EMP001", "202501", "ORD", 1, now),
                new CalculationRunMessage(2L, 99L, "B", "WARN", "kept as-is from repository", "{}", "ESP", "INTERNAL", "EMP001", "202501", "ORD", 1, now.plusSeconds(1))
        ));

        List<CalculationRunMessage> result = service.listByRunId(7L);

        assertEquals(2, result.size());
        assertEquals("A", result.getFirst().messageCode());
        assertEquals("B", result.get(1).messageCode());
        verify(calculationRunMessageRepository).findByRunIdOrderByCreatedAtAscIdAsc(7L);
    }

    @Test
    void rejectsInvalidRunId() {
        assertThrows(InvalidPayrollArgumentException.class, () -> service.listByRunId(0L));
    }
}