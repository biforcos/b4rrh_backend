package com.b4rrhh.employee.lifecycle.application.participant;

import com.b4rrhh.employee.lifecycle.application.model.TerminationContext;
import com.b4rrhh.employee.lifecycle.domain.exception.TerminateEmployeeConflictException;
import com.b4rrhh.employee.working_time.application.usecase.CloseWorkingTimeCommand;
import com.b4rrhh.employee.working_time.application.usecase.CloseWorkingTimeUseCase;
import com.b4rrhh.employee.working_time.application.usecase.ListEmployeeWorkingTimesCommand;
import com.b4rrhh.employee.working_time.application.usecase.ListEmployeeWorkingTimesUseCase;
import com.b4rrhh.employee.working_time.domain.exception.WorkingTimeAlreadyClosedException;
import com.b4rrhh.employee.working_time.domain.model.WorkingTime;
import com.b4rrhh.employee.working_time.domain.model.WorkingTimeDerivedHours;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingTimeTerminationParticipantTest {

    @Mock private ListEmployeeWorkingTimesUseCase listWorkingTimes;
    @Mock private CloseWorkingTimeUseCase closeWorkingTime;

    private static final LocalDate TERMINATION_DATE = LocalDate.of(2026, 3, 31);
    private static final LocalDate START_DATE = LocalDate.of(2026, 1, 1);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 0, 0);

    @Test
    void orderIs10() {
        assertEquals(10, participant().order());
    }

    @Test
    void closesActiveWorkingTimeAndStoresInContext() {
        WorkingTime active = workingTime(1, START_DATE, null);
        WorkingTime closed = workingTime(1, START_DATE, TERMINATION_DATE);
        when(listWorkingTimes.listByEmployeeBusinessKey(any())).thenReturn(List.of(active));
        when(closeWorkingTime.close(any())).thenReturn(closed);

        TerminationContext ctx = context();

        participant().participate(ctx);

        ArgumentCaptor<CloseWorkingTimeCommand> captor =
                ArgumentCaptor.forClass(CloseWorkingTimeCommand.class);
        verify(closeWorkingTime).close(captor.capture());
        assertEquals("ESP", captor.getValue().ruleSystemCode());
        assertEquals("INTERNAL", captor.getValue().employeeTypeCode());
        assertEquals("EMP001", captor.getValue().employeeNumber());
        assertEquals(1, captor.getValue().workingTimeNumber());
        assertEquals(TERMINATION_DATE, captor.getValue().endDate());
        verify(ctx).setClosedWorkingTime(closed);
    }

    @Test
    void skipsWhenNoActiveWorkingTime() {
        WorkingTime onlyClosed = workingTime(1, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));
        when(listWorkingTimes.listByEmployeeBusinessKey(any())).thenReturn(List.of(onlyClosed));

        TerminationContext ctx = context();
        participant().participate(ctx);

        verify(closeWorkingTime, never()).close(any());
        verify(ctx, never()).setClosedWorkingTime(any());
    }

    @Test
    void skipsWhenActiveStartDateIsAfterTerminationDate() {
        WorkingTime future = workingTime(1, LocalDate.of(2026, 4, 1), null);
        when(listWorkingTimes.listByEmployeeBusinessKey(any())).thenReturn(List.of(future));

        participant().participate(context());

        verify(closeWorkingTime, never()).close(any());
    }

    @Test
    void throwsWhenMultipleActiveWorkingTimes() {
        WorkingTime wt1 = workingTime(1, START_DATE, null);
        WorkingTime wt2 = workingTime(2, START_DATE, null);
        when(listWorkingTimes.listByEmployeeBusinessKey(any())).thenReturn(List.of(wt1, wt2));

        assertThrows(TerminateEmployeeConflictException.class,
                () -> participant().participate(context()));
    }

    @Test
    void translatesWorkingTimeAlreadyClosedToConflictException() {
        WorkingTime active = workingTime(1, START_DATE, null);
        when(listWorkingTimes.listByEmployeeBusinessKey(any())).thenReturn(List.of(active));
        when(closeWorkingTime.close(any()))
                .thenThrow(new WorkingTimeAlreadyClosedException(1));

        TerminateEmployeeConflictException ex = assertThrows(
                TerminateEmployeeConflictException.class,
                () -> participant().participate(context()));
        assertNotNull(ex.getCause());
    }

    // --- helpers ---

    private WorkingTimeTerminationParticipant participant() {
        return new WorkingTimeTerminationParticipant(listWorkingTimes, closeWorkingTime);
    }

    private TerminationContext context() {
        TerminationContext ctx = mock(TerminationContext.class);
        when(ctx.ruleSystemCode()).thenReturn("ESP");
        when(ctx.employeeTypeCode()).thenReturn("INTERNAL");
        when(ctx.employeeNumber()).thenReturn("EMP001");
        when(ctx.terminationDate()).thenReturn(TERMINATION_DATE);
        return ctx;
    }

    private WorkingTime workingTime(int number, LocalDate startDate, LocalDate endDate) {
        return WorkingTime.rehydrate(
                (long) number, 100L, number, startDate, endDate,
                new BigDecimal("75"),
                new WorkingTimeDerivedHours(
                        new BigDecimal("30"), new BigDecimal("6"), new BigDecimal("130")),
                NOW, NOW);
    }
}
