package com.b4rrhh.employee.working_time.application.usecase;

public record GetWorkingTimeByBusinessKeyCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        Integer workingTimeNumber
) {
}