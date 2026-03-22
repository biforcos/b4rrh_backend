package com.b4rrhh.employee.workcenter.application.usecase;

import com.b4rrhh.employee.workcenter.domain.model.WorkCenterAssignment;

import java.util.List;

public interface ListWorkCenterAssignmentsUseCase {

    List<WorkCenterAssignment> execute(ListWorkCenterAssignmentsQuery query);
}
