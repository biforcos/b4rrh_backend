package com.b4rrhh.employee.workcenter.application.usecase;

import com.b4rrhh.employee.workcenter.domain.model.WorkCenterAssignment;

import java.util.Optional;

public interface GetWorkCenterAssignmentUseCase {

    Optional<WorkCenterAssignment> execute(GetWorkCenterAssignmentQuery query);
}
