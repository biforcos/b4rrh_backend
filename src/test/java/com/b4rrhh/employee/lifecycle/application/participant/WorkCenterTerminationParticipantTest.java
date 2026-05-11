package com.b4rrhh.employee.lifecycle.application.participant;

import com.b4rrhh.employee.lifecycle.application.model.TerminationContext;
import com.b4rrhh.employee.lifecycle.domain.exception.TerminateEmployeeCatalogValueInvalidException;
import com.b4rrhh.employee.lifecycle.domain.exception.TerminateEmployeeConflictException;
import com.b4rrhh.employee.workcenter.application.usecase.CloseWorkCenterCommand;
import com.b4rrhh.employee.workcenter.application.usecase.CloseWorkCenterUseCase;
import com.b4rrhh.employee.workcenter.application.usecase.ListEmployeeWorkCentersUseCase;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterAlreadyClosedException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterCatalogValueInvalidException;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkCenterTerminationParticipantTest {

    @Mock private ListEmployeeWorkCentersUseCase listWorkCenters;
    @Mock private CloseWorkCenterUseCase closeWorkCenter;

    private static final LocalDate TERMINATION_DATE = LocalDate.of(2026, 3, 31);
    private static final LocalDate START_DATE = LocalDate.of(2026, 1, 1);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 0, 0);

    @Test
    void orderIs20() {
        assertEquals(20, participant().order());
    }

    @Test
    void closesActiveWorkCenterAndStoresInContext() {
        WorkCenter active = workCenter(1, START_DATE, null);
        WorkCenter closed = workCenter(1, START_DATE, TERMINATION_DATE);
        when(listWorkCenters.listByEmployeeBusinessKey(any(), any(), any())).thenReturn(List.of(active));
        when(closeWorkCenter.close(any())).thenReturn(closed);

        TerminationContext ctx = context();

        participant().participate(ctx);

        ArgumentCaptor<CloseWorkCenterCommand> captor =
                ArgumentCaptor.forClass(CloseWorkCenterCommand.class);
        verify(closeWorkCenter).close(captor.capture());
        assertEquals("ESP", captor.getValue().ruleSystemCode());
        assertEquals("INTERNAL", captor.getValue().employeeTypeCode());
        assertEquals("EMP001", captor.getValue().employeeNumber());
        assertEquals(1, captor.getValue().workCenterAssignmentNumber());
        assertEquals(TERMINATION_DATE, captor.getValue().endDate());
        verify(ctx).setClosedWorkCenter(closed);
    }

    @Test
    void skipsWhenNoActiveWorkCenter() {
        WorkCenter onlyClosed = workCenter(1, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));
        when(listWorkCenters.listByEmployeeBusinessKey(any(), any(), any())).thenReturn(List.of(onlyClosed));

        TerminationContext ctx = context();
        participant().participate(ctx);

        verify(closeWorkCenter, never()).close(any());
        verify(ctx, never()).setClosedWorkCenter(any());
    }

    @Test
    void skipsWhenActiveStartDateIsAfterTerminationDate() {
        WorkCenter future = workCenter(1, LocalDate.of(2026, 4, 1), null);
        when(listWorkCenters.listByEmployeeBusinessKey(any(), any(), any())).thenReturn(List.of(future));

        participant().participate(context());

        verify(closeWorkCenter, never()).close(any());
    }

    @Test
    void throwsWhenMultipleActiveWorkCenters() {
        WorkCenter wc1 = workCenter(1, START_DATE, null);
        WorkCenter wc2 = workCenter(2, START_DATE, null);
        when(listWorkCenters.listByEmployeeBusinessKey(any(), any(), any())).thenReturn(List.of(wc1, wc2));

        assertThrows(TerminateEmployeeConflictException.class,
                () -> participant().participate(context()));
    }

    @Test
    void translatesWorkCenterAlreadyClosedToConflictException() {
        WorkCenter active = workCenter(1, START_DATE, null);
        when(listWorkCenters.listByEmployeeBusinessKey(any(), any(), any())).thenReturn(List.of(active));
        when(closeWorkCenter.close(any()))
                .thenThrow(new WorkCenterAlreadyClosedException(1));

        TerminateEmployeeConflictException ex = assertThrows(
                TerminateEmployeeConflictException.class,
                () -> participant().participate(context()));
        assertNotNull(ex.getCause());
    }

    @Test
    void translatesCatalogExceptionToCatalogInvalidException() {
        WorkCenter active = workCenter(1, START_DATE, null);
        when(listWorkCenters.listByEmployeeBusinessKey(any(), any(), any())).thenReturn(List.of(active));
        when(closeWorkCenter.close(any()))
                .thenThrow(new WorkCenterCatalogValueInvalidException("workCenterCode", "INVALID"));

        TerminateEmployeeCatalogValueInvalidException ex = assertThrows(
                TerminateEmployeeCatalogValueInvalidException.class,
                () -> participant().participate(context()));
        assertNotNull(ex.getCause());
    }

    // --- helpers ---

    private WorkCenterTerminationParticipant participant() {
        return new WorkCenterTerminationParticipant(listWorkCenters, closeWorkCenter);
    }

    private TerminationContext context() {
        TerminationContext ctx = mock(TerminationContext.class);
        when(ctx.ruleSystemCode()).thenReturn("ESP");
        when(ctx.employeeTypeCode()).thenReturn("INTERNAL");
        when(ctx.employeeNumber()).thenReturn("EMP001");
        when(ctx.terminationDate()).thenReturn(TERMINATION_DATE);
        return ctx;
    }

    private WorkCenter workCenter(int number, LocalDate startDate, LocalDate endDate) {
        return new WorkCenter(
                (long) number,
                100L,
                number,
                "WC001",
                startDate,
                endDate,
                NOW,
                NOW);
    }
}
