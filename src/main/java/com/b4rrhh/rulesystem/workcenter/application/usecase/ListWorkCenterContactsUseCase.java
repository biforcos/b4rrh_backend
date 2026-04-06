package com.b4rrhh.rulesystem.workcenter.application.usecase;

import com.b4rrhh.rulesystem.workcenter.domain.model.WorkCenterContact;

import java.util.List;

public interface ListWorkCenterContactsUseCase {

    List<WorkCenterContact> list(String ruleSystemCode, String workCenterCode);
}