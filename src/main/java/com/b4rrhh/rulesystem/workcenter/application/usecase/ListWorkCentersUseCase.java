package com.b4rrhh.rulesystem.workcenter.application.usecase;

import com.b4rrhh.rulesystem.workcenter.application.view.WorkCenterDetails;

import java.util.List;

public interface ListWorkCentersUseCase {

    List<WorkCenterDetails> list(ListWorkCentersQuery query);
}