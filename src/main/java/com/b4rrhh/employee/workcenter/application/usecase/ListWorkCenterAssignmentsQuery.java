package com.b4rrhh.employee.workcenter.application.usecase;

public record ListWorkCenterAssignmentsQuery(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber
) {
}
