package com.b4rrhh.rulesystem.workcenter.application.usecase;

import com.b4rrhh.rulesystem.workcenter.domain.model.WorkCenterContact;

public interface UpdateWorkCenterContactUseCase {

    WorkCenterContact update(UpdateWorkCenterContactCommand command);
}