package com.b4rrhh.rulesystem.workcenter.application.usecase;

public record UpdateWorkCenterContactCommand(
        String ruleSystemCode,
        String workCenterCode,
        Integer contactNumber,
        String contactTypeCode,
        String contactValue
) {
}