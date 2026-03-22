package com.b4rrhh.employee.workcenter.application.usecase;

import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenterAssignment;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListWorkCenterAssignmentsService implements ListWorkCenterAssignmentsUseCase {

    private final ListEmployeeWorkCentersUseCase delegate;

    public ListWorkCenterAssignmentsService(ListEmployeeWorkCentersUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<WorkCenterAssignment> execute(ListWorkCenterAssignmentsQuery query) {
        return delegate.listByEmployeeBusinessKey(
                        query.ruleSystemCode(),
                        query.employeeTypeCode(),
                        query.employeeNumber()
                )
                .stream()
                .map(this::toAssignment)
                .toList();
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
