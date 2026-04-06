package com.b4rrhh.rulesystem.workcenter.application.view;

import com.b4rrhh.rulesystem.workcenter.domain.model.WorkCenter;
import com.b4rrhh.rulesystem.workcenter.domain.model.WorkCenterProfile;

public record WorkCenterDetails(
        WorkCenter workCenter,
        WorkCenterProfile profile
) {
}