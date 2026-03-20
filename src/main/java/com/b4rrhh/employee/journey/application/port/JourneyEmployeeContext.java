package com.b4rrhh.employee.journey.application.port;

public record JourneyEmployeeContext(
        Long employeeId,
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String displayName
) {
}
