package com.b4rrhh.employee.lifecycle.application.participant;

import com.b4rrhh.employee.employee.application.usecase.CreateEmployeeCommand;
import com.b4rrhh.employee.employee.application.usecase.CreateEmployeeUseCase;
import com.b4rrhh.employee.lifecycle.application.model.HireContext;
import com.b4rrhh.employee.lifecycle.application.port.HireParticipant;
import org.springframework.stereotype.Component;

@Component
public class EmployeeCoreParticipant implements HireParticipant {

    private final CreateEmployeeUseCase createEmployeeUseCase;

    public EmployeeCoreParticipant(CreateEmployeeUseCase createEmployeeUseCase) {
        this.createEmployeeUseCase = createEmployeeUseCase;
    }

    @Override
    public int order() { return 10; }

    @Override
    public void participate(HireContext ctx) {
        ctx.setEmployee(createEmployeeUseCase.create(new CreateEmployeeCommand(
                ctx.ruleSystemCode(), ctx.employeeTypeCode(), ctx.employeeNumber(),
                ctx.firstName(), ctx.lastName1(), ctx.lastName2(), ctx.preferredName()
        )));
    }
}
