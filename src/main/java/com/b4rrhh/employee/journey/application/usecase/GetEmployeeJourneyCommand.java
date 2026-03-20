package com.b4rrhh.employee.journey.application.usecase;

public record GetEmployeeJourneyCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber
) {
}
