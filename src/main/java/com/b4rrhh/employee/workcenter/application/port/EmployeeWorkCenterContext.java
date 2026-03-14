package com.b4rrhh.employee.workcenter.application.port;

public record EmployeeWorkCenterContext(
        Long employeeId,
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber
) {
}