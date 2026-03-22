package com.b4rrhh.employee.workcenter.application.usecase;

public record GetWorkCenterAssignmentQuery(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        Integer workCenterAssignmentNumber
) {
}
