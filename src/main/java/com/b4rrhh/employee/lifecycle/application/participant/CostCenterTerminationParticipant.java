package com.b4rrhh.employee.lifecycle.application.participant;

import com.b4rrhh.employee.cost_center.application.usecase.CloseActiveCostCenterDistributionAtTerminationUseCase;
import com.b4rrhh.employee.lifecycle.application.model.TerminationContext;
import com.b4rrhh.employee.lifecycle.application.port.TerminationParticipant;
import org.springframework.stereotype.Component;

@Component
public class CostCenterTerminationParticipant implements TerminationParticipant {

    private final CloseActiveCostCenterDistributionAtTerminationUseCase closeIfPresent;

    public CostCenterTerminationParticipant(
            CloseActiveCostCenterDistributionAtTerminationUseCase closeIfPresent) {
        this.closeIfPresent = closeIfPresent;
    }

    @Override
    public int order() { return 30; }

    @Override
    public void participate(TerminationContext ctx) {
        closeIfPresent.closeIfPresent(
                ctx.ruleSystemCode(),
                ctx.employeeTypeCode(),
                ctx.employeeNumber(),
                ctx.terminationDate());
    }
}
