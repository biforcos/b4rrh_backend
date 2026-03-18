package com.b4rrhh.employee.cost_center.application.port;

public record EmployeeCostCenterContext(
        Long employeeId,
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber
) {
}
