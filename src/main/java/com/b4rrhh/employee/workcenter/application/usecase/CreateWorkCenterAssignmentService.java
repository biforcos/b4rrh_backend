package com.b4rrhh.employee.workcenter.application.usecase;

import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenterAssignment;
import org.springframework.stereotype.Service;

@Service
public class CreateWorkCenterAssignmentService implements CreateWorkCenterAssignmentUseCase {

    private final CreateWorkCenterUseCase delegate;

    public CreateWorkCenterAssignmentService(CreateWorkCenterUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    public WorkCenterAssignment execute(CreateWorkCenterAssignmentCommand command) {
        WorkCenter created = delegate.create(new CreateWorkCenterCommand(
                command.ruleSystemCode(),
                command.employeeTypeCode(),
                command.employeeNumber(),
                command.workCenterCode(),
                command.startDate(),
                command.endDate()
        ));

        return toAssignment(created);
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
