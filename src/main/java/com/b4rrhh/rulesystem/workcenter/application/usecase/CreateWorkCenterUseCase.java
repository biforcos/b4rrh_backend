package com.b4rrhh.rulesystem.workcenter.application.usecase;

import com.b4rrhh.rulesystem.workcenter.application.view.WorkCenterDetails;

public interface CreateWorkCenterUseCase {

    WorkCenterDetails create(CreateWorkCenterCommand command);
}