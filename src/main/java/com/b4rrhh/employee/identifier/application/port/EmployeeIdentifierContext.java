package com.b4rrhh.employee.identifier.application.port;

public record EmployeeIdentifierContext(
        Long employeeId,
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber
) {
}
