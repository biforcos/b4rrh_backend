package com.b4rrhh.employee.cost_center.application.usecase;

import com.b4rrhh.employee.cost_center.application.port.CostCenterPresenceConsistencyPort;
import com.b4rrhh.employee.cost_center.application.port.EmployeeCostCenterContext;
import com.b4rrhh.employee.cost_center.application.port.EmployeeCostCenterLookupPort;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterDistributionInvalidException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterDistributionNotFoundException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterEmployeeNotFoundException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterOutsidePresencePeriodException;
import com.b4rrhh.employee.cost_center.domain.model.CostCenterAllocation;
import com.b4rrhh.employee.cost_center.domain.model.CostCenterDistributionWindow;
import com.b4rrhh.employee.cost_center.domain.port.CostCenterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloseCostCenterDistributionServiceTest {

    private static final String RSC = "ESP";
    private static final String ETC = "INTERNAL";
    private static final String EN = "EMP001";
    private static final Long EMPLOYEE_ID = 10L;
    private static final LocalDate WINDOW_START = LocalDate.of(2026, 1, 1);
    private static final LocalDate END_DATE = LocalDate.of(2026, 3, 31);

    @Mock
    private CostCenterRepository costCenterRepository;
    @Mock
    private EmployeeCostCenterLookupPort employeeCostCenterLookupPort;
    @Mock
    private CostCenterPresenceConsistencyPort costCenterPresenceConsistencyPort;

    private CloseCostCenterDistributionService service;

    @BeforeEach
    void setUp() {
        service = new CloseCostCenterDistributionService(
                costCenterRepository,
                employeeCostCenterLookupPort,
                costCenterPresenceConsistencyPort
        );
    }

    // Test 10: close window closes all allocation lines in the window
    @Test
    void closesAllLinesInWindowAndReturnsClosed() {
        givenEmployeeFound();
        CostCenterAllocation lineA = new CostCenterAllocation(
                EMPLOYEE_ID, "CC_A", new BigDecimal("60"), WINDOW_START, null
        );
        CostCenterAllocation lineB = new CostCenterAllocation(
                EMPLOYEE_ID, "CC_B", new BigDecimal("40"), WINDOW_START, null
        );
        when(costCenterRepository.findByEmployeeIdAndStartDate(EMPLOYEE_ID, WINDOW_START))
                .thenReturn(List.of(lineA, lineB));
        when(costCenterPresenceConsistencyPort.existsPresenceContainingPeriod(EMPLOYEE_ID, WINDOW_START, END_DATE))
                .thenReturn(true);

        CostCenterDistributionWindow result = service.close(command(WINDOW_START, END_DATE));

        assertNotNull(result);
        assertEquals(WINDOW_START, result.getStartDate());
        assertEquals(END_DATE, result.getEndDate());
        assertEquals(2, result.getItems().size());
        assertFalse(result.isActive());

        verify(costCenterRepository).closeAllForWindow(EMPLOYEE_ID, WINDOW_START, END_DATE);
    }

    @Test
    void rejectsWhenWindowNotFound() {
        givenEmployeeFound();
        when(costCenterRepository.findByEmployeeIdAndStartDate(EMPLOYEE_ID, WINDOW_START))
                .thenReturn(List.of());

        assertThrows(CostCenterDistributionNotFoundException.class, () ->
                service.close(command(WINDOW_START, END_DATE))
        );
        verify(costCenterRepository, never()).closeAllForWindow(any(), any(), any());
    }

    @Test
    void rejectsWhenEndDateBeforeWindowStartDate() {
        assertThrows(CostCenterDistributionInvalidException.class, () ->
                service.close(command(WINDOW_START, WINDOW_START.minusDays(1)))
        );
        verify(costCenterRepository, never()).findByEmployeeIdAndStartDate(any(), any());
    }

    @Test
    void rejectsWhenPeriodIsOutsidePresence() {
        givenEmployeeFound();
        CostCenterAllocation line = new CostCenterAllocation(
                EMPLOYEE_ID, "CC_A", new BigDecimal("100"), WINDOW_START, null
        );
        when(costCenterRepository.findByEmployeeIdAndStartDate(EMPLOYEE_ID, WINDOW_START))
                .thenReturn(List.of(line));
        when(costCenterPresenceConsistencyPort.existsPresenceContainingPeriod(EMPLOYEE_ID, WINDOW_START, END_DATE))
                .thenReturn(false);

        assertThrows(CostCenterOutsidePresencePeriodException.class, () ->
                service.close(command(WINDOW_START, END_DATE))
        );
        verify(costCenterRepository, never()).closeAllForWindow(any(), any(), any());
    }

    @Test
    void rejectsWhenEmployeeNotFound() {
        when(employeeCostCenterLookupPort.findByBusinessKeyForUpdate(RSC, ETC, EN))
                .thenReturn(Optional.empty());

        assertThrows(CostCenterEmployeeNotFoundException.class, () ->
                service.close(command(WINDOW_START, END_DATE))
        );
    }

    // helpers

    private void givenEmployeeFound() {
        when(employeeCostCenterLookupPort.findByBusinessKeyForUpdate(RSC, ETC, EN))
                .thenReturn(Optional.of(new EmployeeCostCenterContext(EMPLOYEE_ID, RSC, ETC, EN)));
    }

    private CloseCostCenterDistributionCommand command(LocalDate windowStartDate, LocalDate endDate) {
        return new CloseCostCenterDistributionCommand(RSC, ETC, EN, windowStartDate, endDate);
    }
}
