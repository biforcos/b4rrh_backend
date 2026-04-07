package com.b4rrhh.employee.working_time.application.port;

public record EmployeeWorkingTimeContext(
        Long employeeId,
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber
) {
}