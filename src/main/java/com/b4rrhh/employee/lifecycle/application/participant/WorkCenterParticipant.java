package com.b4rrhh.employee.lifecycle.application.participant;

import com.b4rrhh.employee.lifecycle.application.model.HireContext;
import com.b4rrhh.employee.lifecycle.application.port.HireParticipant;
import com.b4rrhh.employee.lifecycle.domain.exception.HireEmployeeCatalogValueInvalidException;
import com.b4rrhh.employee.workcenter.application.usecase.CreateWorkCenterCommand;
import com.b4rrhh.employee.workcenter.application.usecase.CreateWorkCenterUseCase;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterCatalogValueInvalidException;
import org.springframework.stereotype.Component;

@Component
public class WorkCenterParticipant implements HireParticipant {

    private final CreateWorkCenterUseCase createWorkCenterUseCase;

    public WorkCenterParticipant(CreateWorkCenterUseCase createWorkCenterUseCase) {
        this.createWorkCenterUseCase = createWorkCenterUseCase;
    }

    @Override
    public int order() {
        return 30;
    }

    @Override
    public void participate(HireContext ctx) {
        try {
            ctx.setWorkCenter(createWorkCenterUseCase.create(new CreateWorkCenterCommand(
                    ctx.ruleSystemCode(), ctx.employeeTypeCode(), ctx.employeeNumber(),
                    ctx.workCenterCode(), ctx.hireDate(), null
            )));
        } catch (WorkCenterCatalogValueInvalidException ex) {
            throw new HireEmployeeCatalogValueInvalidException(ex.getMessage(), ex);
        }
    }
}
