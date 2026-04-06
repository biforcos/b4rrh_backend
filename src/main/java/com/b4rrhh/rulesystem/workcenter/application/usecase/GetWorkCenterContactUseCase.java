package com.b4rrhh.rulesystem.workcenter.application.usecase;

import com.b4rrhh.rulesystem.workcenter.domain.model.WorkCenterContact;

public interface GetWorkCenterContactUseCase {

    WorkCenterContact get(GetWorkCenterContactQuery query);
}