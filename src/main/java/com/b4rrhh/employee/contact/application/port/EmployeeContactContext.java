package com.b4rrhh.employee.contact.application.port;

public record EmployeeContactContext(
        Long employeeId,
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber
) {
}
