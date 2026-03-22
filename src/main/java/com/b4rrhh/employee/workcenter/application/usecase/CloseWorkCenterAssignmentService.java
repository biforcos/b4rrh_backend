package com.b4rrhh.employee.workcenter.application.usecase;

import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenterAssignment;
import org.springframework.stereotype.Service;

@Service
public class CloseWorkCenterAssignmentService implements CloseWorkCenterAssignmentUseCase {

    private final CloseWorkCenterUseCase delegate;

    public CloseWorkCenterAssignmentService(CloseWorkCenterUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    public WorkCenterAssignment execute(CloseWorkCenterAssignmentCommand command) {
        WorkCenter closed = delegate.close(new CloseWorkCenterCommand(
                command.ruleSystemCode(),
                command.employeeTypeCode(),
                command.employeeNumber(),
                command.workCenterAssignmentNumber(),
                command.endDate()
        ));

        return toAssignment(closed);
    }

    private WorkCenterAssignment toAssignment(WorkCenter workCenter) {
        return new WorkCenterAssignment(
                workCenter.getId(),
                workCenter.getEmployeeId(),
                workCenter.getWorkCenterAssignmentNumber(),
                workCenter.getWorkCenterCode(),
                workCenter.getStartDate(),
                workCenter.getEndDate(),
                workCenter.getCreatedAt(),
                workCenter.getUpdatedAt()
        );
    }
}
