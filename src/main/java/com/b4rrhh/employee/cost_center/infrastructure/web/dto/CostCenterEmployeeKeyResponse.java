package com.b4rrhh.employee.cost_center.infrastructure.web.dto;

public record CostCenterEmployeeKeyResponse(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber
) {
}
