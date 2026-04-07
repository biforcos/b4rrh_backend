package com.b4rrhh.employee.working_time.application.usecase;

public record ListEmployeeWorkingTimesCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber
) {
}