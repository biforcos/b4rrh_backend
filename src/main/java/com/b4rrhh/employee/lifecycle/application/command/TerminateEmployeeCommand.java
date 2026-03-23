package com.b4rrhh.employee.lifecycle.application.command;

import java.time.LocalDate;

public record TerminateEmployeeCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        LocalDate terminationDate,
        String exitReasonCode
) {
}
