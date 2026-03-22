package com.b4rrhh.employee.workcenter.application.usecase;

import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenterAssignment;
import org.springframework.stereotype.Service;

@Service
public class UpdateWorkCenterAssignmentService implements UpdateWorkCenterAssignmentUseCase {

    private final UpdateWorkCenterUseCase delegate;

    public UpdateWorkCenterAssignmentService(UpdateWorkCenterUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    public WorkCenterAssignment execute(UpdateWorkCenterAssignmentCommand command) {
        WorkCenter corrected = delegate.update(new UpdateWorkCenterCommand(
                command.ruleSystemCode(),
                command.employeeTypeCode(),
                command.employeeNumber(),
                command.workCenterAssignmentNumber(),
                command.workCenterCode(),
                command.startDate(),
                command.endDate()
        ));

        return toAssignment(corrected);
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
