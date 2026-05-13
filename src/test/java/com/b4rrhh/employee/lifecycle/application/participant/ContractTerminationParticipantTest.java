package com.b4rrhh.employee.lifecycle.application.participant;

import com.b4rrhh.employee.contract.application.command.CloseContractCommand;
import com.b4rrhh.employee.contract.application.command.ListEmployeeContractsCommand;
import com.b4rrhh.employee.contract.application.usecase.CloseContractUseCase;
import com.b4rrhh.employee.contract.application.usecase.ListEmployeeContractsUseCase;
import com.b4rrhh.employee.contract.domain.exception.ContractAlreadyClosedException;
import com.b4rrhh.employee.contract.domain.model.Contract;
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
class ContractTerminationParticipantTest {

    @Mock private ListEmployeeContractsUseCase listContracts;
    @Mock private CloseContractUseCase closeContract;

    private static final LocalDate TERMINATION_DATE = LocalDate.of(2026, 3, 31);
    private static final LocalDate START_DATE = LocalDate.of(2026, 1, 1);

    @Test
    void orderIs40() {
        assertEquals(40, participant().order());
    }

    @Test
    void closesActiveContractAndStoresInContext() {
        Contract active = contract(START_DATE, null);
        Contract closed = contract(START_DATE, TERMINATION_DATE);
        when(listContracts.listByEmployeeBusinessKey(any())).thenReturn(List.of(active));
        when(closeContract.close(any())).thenReturn(closed);

        TerminationContext ctx = context();

        participant().participate(ctx);

        ArgumentCaptor<CloseContractCommand> captor =
                ArgumentCaptor.forClass(CloseContractCommand.class);
        verify(closeContract).close(captor.capture());
        assertEquals("ESP", captor.getValue().ruleSystemCode());
        assertEquals("INTERNAL", captor.getValue().employeeTypeCode());
        assertEquals("EMP001", captor.getValue().employeeNumber());
        assertEquals(START_DATE, captor.getValue().startDate());
        assertEquals(TERMINATION_DATE, captor.getValue().endDate());
        verify(ctx).setClosedContract(closed);
    }

    @Test
    void skipsWhenNoActiveContract() {
        Contract onlyClosed = contract(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));
        when(listContracts.listByEmployeeBusinessKey(any())).thenReturn(List.of(onlyClosed));

        TerminationContext ctx = context();
        participant().participate(ctx);

        verify(closeContract, never()).close(any());
        verify(ctx, never()).setClosedContract(any());
    }

    @Test
    void skipsWhenActiveStartDateIsAfterTerminationDate() {
        Contract future = contract(LocalDate.of(2026, 4, 1), null);
        when(listContracts.listByEmployeeBusinessKey(any())).thenReturn(List.of(future));

        participant().participate(context());

        verify(closeContract, never()).close(any());
    }

    @Test
    void throwsWhenMultipleActiveContracts() {
        Contract c1 = contract(START_DATE, null);
        Contract c2 = contract(LocalDate.of(2026, 2, 1), null);
        when(listContracts.listByEmployeeBusinessKey(any())).thenReturn(List.of(c1, c2));

        assertThrows(TerminateEmployeeConflictException.class,
                () -> participant().participate(context()));
    }

    @Test
    void translatesContractExceptionToConflictException() {
        Contract active = contract(START_DATE, null);
        when(listContracts.listByEmployeeBusinessKey(any())).thenReturn(List.of(active));
        when(closeContract.close(any()))
                .thenThrow(new ContractAlreadyClosedException(START_DATE));

        TerminateEmployeeConflictException ex = assertThrows(
                TerminateEmployeeConflictException.class,
                () -> participant().participate(context()));
        assertNotNull(ex.getCause());
    }

    // --- helpers ---

    private ContractTerminationParticipant participant() {
        return new ContractTerminationParticipant(listContracts, closeContract);
    }

    private TerminationContext context() {
        TerminationContext ctx = mock(TerminationContext.class);
        when(ctx.ruleSystemCode()).thenReturn("ESP");
        when(ctx.employeeTypeCode()).thenReturn("INTERNAL");
        when(ctx.employeeNumber()).thenReturn("EMP001");
        when(ctx.terminationDate()).thenReturn(TERMINATION_DATE);
        return ctx;
    }

    private Contract contract(LocalDate startDate, LocalDate endDate) {
        return new Contract(1L, "CNT", "SUB", startDate, endDate);
    }
}
