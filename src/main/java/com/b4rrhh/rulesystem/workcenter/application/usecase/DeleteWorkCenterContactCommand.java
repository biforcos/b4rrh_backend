package com.b4rrhh.rulesystem.workcenter.application.usecase;

public record DeleteWorkCenterContactCommand(String ruleSystemCode, String workCenterCode, Integer contactNumber) {
}