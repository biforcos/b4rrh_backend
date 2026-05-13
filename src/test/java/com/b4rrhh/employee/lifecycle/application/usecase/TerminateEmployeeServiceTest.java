package com.b4rrhh.employee.lifecycle.application.usecase;

import com.b4rrhh.employee.employee.domain.model.Employee;
import com.b4rrhh.employee.employee.domain.port.EmployeeRepository;
import com.b4rrhh.employee.lifecycle.application.command.TerminateEmployeeCommand;
import com.b4rrhh.employee.lifecycle.application.model.TerminationContext;
import com.b4rrhh.employee.lifecycle.application.port.TerminationParticipant;
import com.b4rrhh.employee.lifecycle.application.service.TerminationPreConditionValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TerminateEmployeeServiceTest {

    @Mock private TerminationPreConditionValidator validator;
    @Mock private EmployeeRepository employeeRepository;

    private final TerminationParticipant firstParticipant = mock(TerminationParticipant.class);
    private final TerminationParticipant secondParticipant = mock(TerminationParticipant.class);

    @Test
    void callsValidatorThenRunsParticipantsInOrder() {
        TerminationContext ctx = mock(TerminationContext.class);
        when(validator.validateAndLookup(any())).thenReturn(ctx);
        when(ctx.isAlreadyTerminated()).thenReturn(false);
        when(ctx.terminatedEmployee()).thenReturn(mock(Employee.class));
        when(firstParticipant.order()).thenReturn(10);
        when(secondParticipant.order()).thenReturn(20);

        service(List.of(secondParticipant, firstParticipant)).terminate(command());

        InOrder order = inOrder(validator, firstParticipant, secondParticipant);
        order.verify(validator).validateAndLookup(any());
        order.verify(firstParticipant).participate(ctx);
        order.verify(secondParticipant).participate(ctx);
    }

    @Test
    void returnsIdempotentResultWhenAlreadyTerminated() {
        TerminationContext ctx = mock(TerminationContext.class);
        when(validator.validateAndLookup(any())).thenReturn(ctx);
        when(ctx.isAlreadyTerminated()).thenReturn(true);
        when(ctx.reconstructIdempotentResult()).thenReturn(null);

        service(List.of(firstParticipant)).terminate(command());

        verify(firstParticipant, never()).participate(any());
        verify(ctx).reconstructIdempotentResult();
    }

    @Test
    void runsPostConditionCheckAfterParticipants() {
        TerminationContext ctx = mock(TerminationContext.class);
        when(validator.validateAndLookup(any())).thenReturn(ctx);
        when(ctx.isAlreadyTerminated()).thenReturn(false);
        when(ctx.terminatedEmployee()).thenReturn(mock(Employee.class));
        when(firstParticipant.order()).thenReturn(10);

        service(List.of(firstParticipant)).terminate(command());

        InOrder order = inOrder(firstParticipant, ctx);
        order.verify(firstParticipant).participate(ctx);
        order.verify(ctx).assertNoActivePresence();
    }

    @Test
    void savesTerminatedEmployeeAfterParticipants() {
        TerminationContext ctx = mock(TerminationContext.class);
        Employee terminatedEmployee = mock(Employee.class);
        when(validator.validateAndLookup(any())).thenReturn(ctx);
        when(ctx.isAlreadyTerminated()).thenReturn(false);
        when(ctx.terminatedEmployee()).thenReturn(terminatedEmployee);

        service(List.of()).terminate(command());

        InOrder order = inOrder(ctx, employeeRepository);
        order.verify(ctx).assertNoActivePresence();
        order.verify(employeeRepository).save(terminatedEmployee);
    }

    // --- helpers ---

    private TerminateEmployeeService service(List<TerminationParticipant> participants) {
        return new TerminateEmployeeService(validator, participants, employeeRepository);
    }

    private TerminateEmployeeCommand command() {
        return new TerminateEmployeeCommand(
                "ESP", "INTERNAL", "EMP001", LocalDate.of(2026, 3, 31), "VOL");
    }
}
