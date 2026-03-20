package com.b4rrhh.employee.journey.infrastructure.web.dto;

public record JourneyEmployeeHeaderResponse(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String displayName
) {
}
