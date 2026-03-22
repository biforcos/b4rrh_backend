package com.b4rrhh.employee.workcenter.application.usecase;

public record DeleteWorkCenterCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        Integer workCenterAssignmentNumber
) {
}