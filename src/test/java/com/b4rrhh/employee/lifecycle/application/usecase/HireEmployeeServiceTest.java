package com.b4rrhh.employee.lifecycle.application.usecase;

import com.b4rrhh.employee.lifecycle.application.command.HireEmployeeCommand;
import com.b4rrhh.employee.lifecycle.application.model.HireContext;
import com.b4rrhh.employee.lifecycle.application.port.HireParticipant;
import com.b4rrhh.employee.lifecycle.application.port.NextEmployeeNumberPort;
import com.b4rrhh.employee.lifecycle.application.service.HireEmployeePreConditionValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HireEmployeeServiceTest {

    @Mock
    private HireEmployeePreConditionValidator validator;
    @Mock
    private NextEmployeeNumberPort nextEmployeeNumberPort;

    private final HireParticipant firstParticipant = mock(HireParticipant.class);
    private final HireParticipant secondParticipant = mock(HireParticipant.class);

    @Test
    void callsValidatorThenConsumesNumberThenRunsParticipantsInOrder() {
        HireContext ctx = mock(HireContext.class);
        when(validator.validateAndNormalize(any(HireEmployeeCommand.class))).thenReturn(ctx);
        when(ctx.ruleSystemCode()).thenReturn("ESP");
        when(nextEmployeeNumberPort.consumeNext(anyString())).thenReturn("EMP000001");
        when(firstParticipant.order()).thenReturn(10);
        when(secondParticipant.order()).thenReturn(20);

        HireEmployeeService service = new HireEmployeeService(
                validator, nextEmployeeNumberPort, List.of(secondParticipant, firstParticipant));
        service.hire(validCommand());

        InOrder order = inOrder(validator, nextEmployeeNumberPort, firstParticipant, secondParticipant);
        order.verify(validator).validateAndNormalize(any(HireEmployeeCommand.class));
        order.verify(nextEmployeeNumberPort).consumeNext("ESP");
        order.verify(firstParticipant).participate(ctx);
        order.verify(secondParticipant).participate(ctx);
    }

    @Test
    void setsEmployeeNumberOnContextBeforeRunningParticipants() {
        HireContext ctx = mock(HireContext.class);
        when(validator.validateAndNormalize(any(HireEmployeeCommand.class))).thenReturn(ctx);
        when(ctx.ruleSystemCode()).thenReturn("ESP");
        when(nextEmployeeNumberPort.consumeNext(anyString())).thenReturn("EMP999999");
        when(firstParticipant.order()).thenReturn(10);

        HireEmployeeService service = new HireEmployeeService(
                validator, nextEmployeeNumberPort, List.of(firstParticipant));
        service.hire(validCommand());

        verify(ctx).setEmployeeNumber("EMP999999");
    }

    @Test
    void returnsResultFromContext() {
        HireContext ctx = mock(HireContext.class);
        when(validator.validateAndNormalize(any(HireEmployeeCommand.class))).thenReturn(ctx);
        when(ctx.ruleSystemCode()).thenReturn("ESP");
        when(nextEmployeeNumberPort.consumeNext(any())).thenReturn("EMP000001");

        HireEmployeeService service = new HireEmployeeService(
                validator, nextEmployeeNumberPort, List.of());
        service.hire(validCommand());

        verify(ctx).toResult();
    }

    @Test
    void sortedByOrderRegardlessOfInjectionOrder() {
        HireContext ctx = mock(HireContext.class);
        when(validator.validateAndNormalize(any(HireEmployeeCommand.class))).thenReturn(ctx);
        when(ctx.ruleSystemCode()).thenReturn("ESP");
        when(nextEmployeeNumberPort.consumeNext(anyString())).thenReturn("EMP000001");
        when(firstParticipant.order()).thenReturn(10);
        when(secondParticipant.order()).thenReturn(20);

        HireEmployeeService service = new HireEmployeeService(
                validator, nextEmployeeNumberPort, List.of(secondParticipant, firstParticipant));
        service.hire(validCommand());

        InOrder order = inOrder(firstParticipant, secondParticipant);
        order.verify(firstParticipant).participate(ctx);
        order.verify(secondParticipant).participate(ctx);
    }

    private HireEmployeeCommand validCommand() {
        return new HireEmployeeCommand(
                "ESP", "INTERNAL", "Ana", "Lopez", null, "Ani",
                LocalDate.of(2026, 3, 23), "HIRE", "COMP", "WC1",
                new HireEmployeeCommand.HireEmployeeContractCommand("CON", "SUB"),
                new HireEmployeeCommand.HireEmployeeLaborClassificationCommand("AGR", "CAT"),
                null,
                new HireEmployeeCommand.HireEmployeeWorkingTimeCommand(new BigDecimal("75"))
        );
    }
}
