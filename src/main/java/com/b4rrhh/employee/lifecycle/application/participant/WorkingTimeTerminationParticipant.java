package com.b4rrhh.employee.lifecycle.application.participant;

import com.b4rrhh.employee.lifecycle.application.model.TerminationContext;
import com.b4rrhh.employee.lifecycle.application.port.TerminationParticipant;
import com.b4rrhh.employee.lifecycle.domain.exception.TerminateEmployeeConflictException;
import com.b4rrhh.employee.working_time.application.usecase.CloseWorkingTimeCommand;
import com.b4rrhh.employee.working_time.application.usecase.CloseWorkingTimeUseCase;
import com.b4rrhh.employee.working_time.application.usecase.ListEmployeeWorkingTimesCommand;
import com.b4rrhh.employee.working_time.application.usecase.ListEmployeeWorkingTimesUseCase;
import com.b4rrhh.employee.working_time.domain.exception.InvalidWorkingTimeDateRangeException;
import com.b4rrhh.employee.working_time.domain.exception.WorkingTimeAlreadyClosedException;
import com.b4rrhh.employee.working_time.domain.exception.WorkingTimeNotFoundException;
import com.b4rrhh.employee.working_time.domain.exception.WorkingTimeOutsidePresencePeriodException;
import com.b4rrhh.employee.working_time.domain.model.WorkingTime;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WorkingTimeTerminationParticipant implements TerminationParticipant {

    private final ListEmployeeWorkingTimesUseCase listWorkingTimes;
    private final CloseWorkingTimeUseCase closeWorkingTime;

    public WorkingTimeTerminationParticipant(
            ListEmployeeWorkingTimesUseCase listWorkingTimes,
            CloseWorkingTimeUseCase closeWorkingTime) {
        this.listWorkingTimes = listWorkingTimes;
        this.closeWorkingTime = closeWorkingTime;
    }

    @Override
    public int order() { return 10; }

    @Override
    public void participate(TerminationContext ctx) {
        List<WorkingTime> all = listWorkingTimes.listByEmployeeBusinessKey(
                new ListEmployeeWorkingTimesCommand(
                        ctx.ruleSystemCode(), ctx.employeeTypeCode(), ctx.employeeNumber()));

        List<WorkingTime> active = all.stream()
                .filter(wt -> wt.getEndDate() == null)
                .toList();

        if (active.size() > 1) {
            throw new TerminateEmployeeConflictException(
                    "Multiple active working times found for employee " + ctx.employeeNumber());
        }
        if (active.isEmpty()) return;

        WorkingTime activeWt = active.get(0);
        if (activeWt.getStartDate().isAfter(ctx.terminationDate())) return;

        try {
            WorkingTime closed = closeWorkingTime.close(new CloseWorkingTimeCommand(
                    ctx.ruleSystemCode(), ctx.employeeTypeCode(), ctx.employeeNumber(),
                    activeWt.getWorkingTimeNumber(), ctx.terminationDate()));
            ctx.setClosedWorkingTime(closed);
        } catch (WorkingTimeAlreadyClosedException | WorkingTimeNotFoundException |
                 InvalidWorkingTimeDateRangeException | WorkingTimeOutsidePresencePeriodException e) {
            throw new TerminateEmployeeConflictException(e.getMessage(), e);
        }
    }
}
