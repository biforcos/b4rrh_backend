package com.b4rrhh.employee.working_time.application.usecase;

import java.time.LocalDate;

public record CloseWorkingTimeCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        Integer workingTimeNumber,
        LocalDate endDate
) {
}