package com.b4rrhh.employee.lifecycle.application.usecase;

import com.b4rrhh.employee.lifecycle.application.model.HireContext;
import com.b4rrhh.employee.lifecycle.application.model.HireEmployeeResult;
import com.b4rrhh.employee.lifecycle.application.command.HireEmployeeCommand;
import com.b4rrhh.employee.lifecycle.application.port.HireParticipant;
import com.b4rrhh.employee.lifecycle.application.port.NextEmployeeNumberPort;
import com.b4rrhh.employee.lifecycle.application.service.HireEmployeePreConditionValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class HireEmployeeService implements HireEmployeeUseCase {

    private final HireEmployeePreConditionValidator validator;
    private final NextEmployeeNumberPort nextEmployeeNumberPort;
    private final List<HireParticipant> participants;

    public HireEmployeeService(
            HireEmployeePreConditionValidator validator,
            NextEmployeeNumberPort nextEmployeeNumberPort,
            List<HireParticipant> participants) {
        this.validator = validator;
        this.nextEmployeeNumberPort = nextEmployeeNumberPort;
        this.participants = participants.stream()
                .sorted(Comparator.comparingInt(HireParticipant::order))
                .toList();
    }

    @Override
    @Transactional
    public HireEmployeeResult hire(HireEmployeeCommand command) {
        HireContext ctx = validator.validateAndNormalize(command);
        ctx.setEmployeeNumber(nextEmployeeNumberPort.consumeNext(ctx.ruleSystemCode()));
        participants.forEach(p -> p.participate(ctx));
        return ctx.toResult();
    }
}
