package com.b4rrhh.employee.lifecycle.application.usecase;

import com.b4rrhh.employee.employee.domain.port.EmployeeRepository;
import com.b4rrhh.employee.lifecycle.application.command.TerminateEmployeeCommand;
import com.b4rrhh.employee.lifecycle.application.model.TerminateEmployeeResult;
import com.b4rrhh.employee.lifecycle.application.model.TerminationContext;
import com.b4rrhh.employee.lifecycle.application.port.TerminationParticipant;
import com.b4rrhh.employee.lifecycle.application.service.TerminationPreConditionValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class TerminateEmployeeService implements TerminateEmployeeUseCase {

    private final TerminationPreConditionValidator validator;
    private final List<TerminationParticipant> participants;
    private final EmployeeRepository employeeRepository;

    public TerminateEmployeeService(
            TerminationPreConditionValidator validator,
            List<TerminationParticipant> participants,
            EmployeeRepository employeeRepository) {
        this.validator = validator;
        this.participants = participants.stream()
                .sorted(Comparator.comparingInt(TerminationParticipant::order))
                .toList();
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public TerminateEmployeeResult terminate(TerminateEmployeeCommand command) {
        TerminationContext ctx = validator.validateAndLookup(command);
        if (ctx.isAlreadyTerminated()) return ctx.reconstructIdempotentResult();
        participants.forEach(p -> p.participate(ctx));
        ctx.assertNoActivePresence();
        employeeRepository.save(ctx.terminatedEmployee());
        return ctx.toResult();
    }
}
