package com.b4rrhh.employee.lifecycle.application.participant;

import com.b4rrhh.employee.labor_classification.application.command.CloseLaborClassificationCommand;
import com.b4rrhh.employee.labor_classification.application.command.ListEmployeeLaborClassificationsCommand;
import com.b4rrhh.employee.labor_classification.application.usecase.CloseLaborClassificationUseCase;
import com.b4rrhh.employee.labor_classification.application.usecase.ListEmployeeLaborClassificationsUseCase;
import com.b4rrhh.employee.labor_classification.domain.model.LaborClassification;
import com.b4rrhh.employee.lifecycle.application.model.TerminationContext;
import com.b4rrhh.employee.lifecycle.application.port.TerminationParticipant;
import com.b4rrhh.employee.lifecycle.domain.exception.TerminateEmployeeConflictException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LaborClassificationTerminationParticipant implements TerminationParticipant {

    private final ListEmployeeLaborClassificationsUseCase listLaborClassifications;
    private final CloseLaborClassificationUseCase closeLaborClassification;

    public LaborClassificationTerminationParticipant(
            ListEmployeeLaborClassificationsUseCase listLaborClassifications,
            CloseLaborClassificationUseCase closeLaborClassification) {
        this.listLaborClassifications = listLaborClassifications;
        this.closeLaborClassification = closeLaborClassification;
    }

    @Override
    public int order() { return 50; }

    @Override
    public void participate(TerminationContext ctx) {
        List<LaborClassification> all = listLaborClassifications.listByEmployeeBusinessKey(
                new ListEmployeeLaborClassificationsCommand(
                        ctx.ruleSystemCode(), ctx.employeeTypeCode(), ctx.employeeNumber()));

        List<LaborClassification> active = all.stream()
                .filter(lc -> lc.getEndDate() == null)
                .toList();

        if (active.size() > 1) {
            throw new TerminateEmployeeConflictException(
                    "Multiple active labor classifications found for employee " + ctx.employeeNumber());
        }
        if (active.isEmpty()) return;

        LaborClassification activeLc = active.get(0);
        if (activeLc.getStartDate().isAfter(ctx.terminationDate())) return;

        try {
            LaborClassification closed = closeLaborClassification.close(new CloseLaborClassificationCommand(
                    ctx.ruleSystemCode(), ctx.employeeTypeCode(), ctx.employeeNumber(),
                    activeLc.getStartDate(), ctx.terminationDate()));
            ctx.setClosedLaborClassification(closed);
        } catch (RuntimeException e) {
            throw new TerminateEmployeeConflictException(e.getMessage(), e);
        }
    }
}