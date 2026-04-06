package com.b4rrhh.rulesystem.workcenter.application.usecase;

public record CreateWorkCenterContactCommand(
        String ruleSystemCode,
        String workCenterCode,
        String contactTypeCode,
        String contactValue
) {
}