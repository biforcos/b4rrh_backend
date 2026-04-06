package com.b4rrhh.rulesystem.workcenter.application.usecase;

public record GetWorkCenterContactQuery(String ruleSystemCode, String workCenterCode, Integer contactNumber) {
}