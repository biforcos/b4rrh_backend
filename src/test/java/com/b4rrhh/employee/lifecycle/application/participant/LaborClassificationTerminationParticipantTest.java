package com.b4rrhh.employee.lifecycle.application.participant;

import com.b4rrhh.employee.labor_classification.application.command.CloseLaborClassificationCommand;
import com.b4rrhh.employee.labor_classification.application.command.ListEmployeeLaborClassificationsCommand;
import com.b4rrhh.employee.labor_classification.application.usecase.CloseLaborClassificationUseCase;
import com.b4rrhh.employee.labor_classification.application.usecase.ListEmployeeLaborClassificationsUseCase;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationAlreadyClosedException;
import com.b4rrhh.employee.labor_classification.domain.model.LaborClassification;
import com.b4rrhh.employee.lifecycle.application.model.TerminationContext;
import com.b4rrhh.employee.lifecycle.domain.exception.TerminateEmployeeConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LaborClassificationTerminationParticipantTest {

    @Mock private ListEmployeeLaborClassificationsUseCase listLaborClassifications;
    @Mock private CloseLaborClassificationUseCase closeLaborClassification;

    private static final LocalDate TERMINATION_DATE = LocalDate.of(2026, 3, 31);
    private static final LocalDate START_DATE = LocalDate.of(2026, 1, 1);

    @Test
    void orderIs50() {
        assertEquals(50, participant().order());
    }

    @Test
    void closesActiveLaborClassificationAndStoresInContext() {
        LaborClassification active = laborClassification(START_DATE, null);
        LaborClassification closed = laborClassification(START_DATE, TERMINATION_DATE);
        when(listLaborClassifications.listByEmployeeBusinessKey(any())).thenReturn(List.of(active));
        when(closeLaborClassification.close(any())).thenReturn(closed);

        TerminationContext ctx = context();

        participant().participate(ctx);

        ArgumentCaptor<CloseLaborClassificationCommand> captor =
                ArgumentCaptor.forClass(CloseLaborClassificationCommand.class);
        verify(closeLaborClassification).close(captor.capture());
        assertEquals("ESP", captor.getValue().ruleSystemCode());
        assertEquals("INTERNAL", captor.getValue().employeeTypeCode());
        assertEquals("EMP001", captor.getValue().employeeNumber());
        assertEquals(START_DATE, captor.getValue().startDate());
        assertEquals(TERMINATION_DATE, captor.getValue().endDate());
        verify(ctx).setClosedLaborClassification(closed);
    }

    @Test
    void skipsWhenNoActiveLaborClassification() {
        LaborClassification onlyClosed = laborClassification(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));
        when(listLaborClassifications.listByEmployeeBusinessKey(any())).thenReturn(List.of(onlyClosed));

        TerminationContext ctx = context();
        participant().participate(ctx);

        verify(closeLaborClassification, never()).close(any());
        verify(ctx, never()).setClosedLaborClassification(any());
    }

    @Test
    void skipsWhenActiveStartDateIsAfterTerminationDate() {
        LaborClassification future = laborClassification(LocalDate.of(2026, 4, 1), null);
        when(listLaborClassifications.listByEmployeeBusinessKey(any())).thenReturn(List.of(future));

        participant().participate(context());

        verify(closeLaborClassification, never()).close(any());
    }

    @Test
    void throwsWhenMultipleActiveLaborClassifications() {
        LaborClassification lc1 = laborClassification(START_DATE, null);
        LaborClassification lc2 = laborClassification(LocalDate.of(2026, 2, 1), null);
        when(listLaborClassifications.listByEmployeeBusinessKey(any())).thenReturn(List.of(lc1, lc2));

        assertThrows(TerminateEmployeeConflictException.class,
                () -> participant().participate(context()));
    }

    @Test
    void translatesLaborClassificationExceptionToConflictException() {
        LaborClassification active = laborClassification(START_DATE, null);
        when(listLaborClassifications.listByEmployeeBusinessKey(any())).thenReturn(List.of(active));
        when(closeLaborClassification.close(any()))
                .thenThrow(new LaborClassificationAlreadyClosedException(START_DATE));

        TerminateEmployeeConflictException ex = assertThrows(
                TerminateEmployeeConflictException.class,
                () -> participant().participate(context()));
        assertNotNull(ex.getCause());
    }

    // --- helpers ---

    private LaborClassificationTerminationParticipant participant() {
        return new LaborClassificationTerminationParticipant(listLaborClassifications, closeLaborClassification);
    }

    private TerminationContext context() {
        TerminationContext ctx = mock(TerminationContext.class);
        when(ctx.ruleSystemCode()).thenReturn("ESP");
        when(ctx.employeeTypeCode()).thenReturn("INTERNAL");
        when(ctx.employeeNumber()).thenReturn("EMP001");
        when(ctx.terminationDate()).thenReturn(TERMINATION_DATE);
        return ctx;
    }

    private LaborClassification laborClassification(LocalDate startDate, LocalDate endDate) {
        return new LaborClassification(1L, "AGR001", "CAT001", startDate, endDate);
    }
}
