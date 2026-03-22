package com.b4rrhh.employee.workcenter.application.usecase;

import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;

public interface UpdateWorkCenterUseCase {

    WorkCenter update(UpdateWorkCenterCommand command);
}
