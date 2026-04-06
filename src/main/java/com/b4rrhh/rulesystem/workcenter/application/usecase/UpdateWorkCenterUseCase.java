package com.b4rrhh.rulesystem.workcenter.application.usecase;

import com.b4rrhh.rulesystem.workcenter.application.view.WorkCenterDetails;

public interface UpdateWorkCenterUseCase {

    WorkCenterDetails update(UpdateWorkCenterCommand command);
}