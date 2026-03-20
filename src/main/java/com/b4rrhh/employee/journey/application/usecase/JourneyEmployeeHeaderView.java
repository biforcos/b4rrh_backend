package com.b4rrhh.employee.journey.application.usecase;

public record JourneyEmployeeHeaderView(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String displayName
) {
}
