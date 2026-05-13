package com.b4rrhh.employee.lifecycle.application.participant;

import com.b4rrhh.employee.cost_center.application.usecase.CloseActiveCostCenterDistributionAtTerminationUseCase;
import com.b4rrhh.employee.lifecycle.application.model.TerminationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CostCenterTerminationParticipantTest {

    @Mock private CloseActiveCostCenterDistributionAtTerminationUseCase closeIfPresent;

    private static final LocalDate TERMINATION_DATE = LocalDate.of(2026, 3, 31);

    @Test
    void orderIs30() {
        assertEquals(30, new CostCenterTerminationParticipant(closeIfPresent).order());
    }

    @Test
    void delegatesToCloseIfPresentWithCorrectArgs() {
        TerminationContext ctx = context();

        participant().participate(ctx);

        verify(closeIfPresent).closeIfPresent("ESP", "INTERNAL", "EMP001", TERMINATION_DATE);
    }

    @Test
    void doesNotSetAnyResultOnContext() {
        TerminationContext ctx = context();

        participant().participate(ctx);

        verify(ctx, never()).setClosedPresence(any());
        verify(ctx, never()).setClosedWorkCenter(any());
        verify(ctx, never()).setClosedContract(any());
        verify(ctx, never()).setClosedLaborClassification(any());
        verify(ctx, never()).setClosedWorkingTime(any());
    }

    // --- helpers ---

    private CostCenterTerminationParticipant participant() {
        return new CostCenterTerminationParticipant(closeIfPresent);
    }

    private TerminationContext context() {
        TerminationContext ctx = mock(TerminationContext.class);
        when(ctx.ruleSystemCode()).thenReturn("ESP");
        when(ctx.employeeTypeCode()).thenReturn("INTERNAL");
        when(ctx.employeeNumber()).thenReturn("EMP001");
        when(ctx.terminationDate()).thenReturn(TERMINATION_DATE);
        return ctx;
    }
}
