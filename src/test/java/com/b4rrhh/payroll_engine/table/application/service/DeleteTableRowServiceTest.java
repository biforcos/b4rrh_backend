package com.b4rrhh.payroll_engine.table.application.service;

import com.b4rrhh.payroll_engine.table.domain.exception.TableRowNotFoundException;
import com.b4rrhh.payroll_engine.table.domain.model.PayrollTableRow;
import com.b4rrhh.payroll_engine.table.domain.port.PayrollTableRowManagementPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteTableRowServiceTest {

    @Mock
    private PayrollTableRowManagementPort port;

    @InjectMocks
    private DeleteTableRowService service;

    @Test
    void deletesRowWhenFound() {
        PayrollTableRow row = new PayrollTableRow(7L, "ESP", "SB_TEST", "SB-G1",
                LocalDate.of(2024, 1, 1), null,
                new BigDecimal("1800.00"), new BigDecimal("21600.00"),
                new BigDecimal("60.00"), new BigDecimal("7.50"), true);
        when(port.findById(7L)).thenReturn(Optional.of(row));

        service.delete(7L);

        verify(port).deleteById(7L);
    }

    @Test
    void throwsNotFoundWhenRowDoesNotExist() {
        when(port.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(TableRowNotFoundException.class);
    }
}
