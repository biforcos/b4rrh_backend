package com.b4rrhh.employee.workcenter.application.usecase;

import com.b4rrhh.employee.workcenter.domain.model.WorkCenterAssignment;

public interface UpdateWorkCenterAssignmentUseCase {

    WorkCenterAssignment execute(UpdateWorkCenterAssignmentCommand command);
}
