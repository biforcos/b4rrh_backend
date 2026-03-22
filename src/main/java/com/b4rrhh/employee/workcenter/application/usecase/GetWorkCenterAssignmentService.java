package com.b4rrhh.employee.workcenter.application.usecase;

import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenterAssignment;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetWorkCenterAssignmentService implements GetWorkCenterAssignmentUseCase {

    private final GetWorkCenterByBusinessKeyUseCase delegate;

    public GetWorkCenterAssignmentService(GetWorkCenterByBusinessKeyUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    public Optional<WorkCenterAssignment> execute(GetWorkCenterAssignmentQuery query) {
        return delegate.getByBusinessKey(
                        query.ruleSystemCode(),
                        query.employeeTypeCode(),
                        query.employeeNumber(),
                        query.workCenterAssignmentNumber()
                )
                .map(this::toAssignment);
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
