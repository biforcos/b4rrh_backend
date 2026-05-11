package com.b4rrhh.employee.lifecycle.application.participant;

import com.b4rrhh.employee.lifecycle.application.model.TerminationContext;
import com.b4rrhh.employee.lifecycle.application.port.TerminationParticipant;
import com.b4rrhh.employee.lifecycle.domain.exception.TerminateEmployeeCatalogValueInvalidException;
import com.b4rrhh.employee.lifecycle.domain.exception.TerminateEmployeeConflictException;
import com.b4rrhh.employee.workcenter.application.usecase.CloseWorkCenterCommand;
import com.b4rrhh.employee.workcenter.application.usecase.CloseWorkCenterUseCase;
import com.b4rrhh.employee.workcenter.application.usecase.ListEmployeeWorkCentersUseCase;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterCatalogValueInvalidException;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WorkCenterTerminationParticipant implements TerminationParticipant {

    private final ListEmployeeWorkCentersUseCase listWorkCenters;
    private final CloseWorkCenterUseCase closeWorkCenter;

    public WorkCenterTerminationParticipant(
            ListEmployeeWorkCentersUseCase listWorkCenters,
            CloseWorkCenterUseCase closeWorkCenter) {
        this.listWorkCenters = listWorkCenters;
        this.closeWorkCenter = closeWorkCenter;
    }

    @Override
    public int order() { return 20; }

    @Override
    public void participate(TerminationContext ctx) {
        List<WorkCenter> all = listWorkCenters.listByEmployeeBusinessKey(
                ctx.ruleSystemCode(), ctx.employeeTypeCode(), ctx.employeeNumber());

        List<WorkCenter> active = all.stream()
                .filter(wc -> wc.getEndDate() == null)
                .toList();

        if (active.size() > 1) {
            throw new TerminateEmployeeConflictException(
                    "Multiple active work centers found for employee " + ctx.employeeNumber());
        }
        if (active.isEmpty()) return;

        WorkCenter activeWc = active.get(0);
        if (activeWc.getStartDate().isAfter(ctx.terminationDate())) return;

        try {
            WorkCenter closed = closeWorkCenter.close(new CloseWorkCenterCommand(
                    ctx.ruleSystemCode(), ctx.employeeTypeCode(), ctx.employeeNumber(),
                    activeWc.getWorkCenterAssignmentNumber(), ctx.terminationDate()));
            ctx.setClosedWorkCenter(closed);
        } catch (WorkCenterCatalogValueInvalidException e) {
            throw new TerminateEmployeeCatalogValueInvalidException(e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new TerminateEmployeeConflictException(e.getMessage(), e);
        }
    }
}
