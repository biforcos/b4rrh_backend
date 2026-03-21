package com.b4rrhh.employee.journey.infrastructure.web.dto.v2;

public record JourneyEmployeeHeader(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String displayName
) {
}