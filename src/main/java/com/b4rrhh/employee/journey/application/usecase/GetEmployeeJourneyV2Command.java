package com.b4rrhh.employee.journey.application.usecase;

public record GetEmployeeJourneyV2Command(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber
) {
}