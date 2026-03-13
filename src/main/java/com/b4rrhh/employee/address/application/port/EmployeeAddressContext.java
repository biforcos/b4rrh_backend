package com.b4rrhh.employee.address.application.port;

public record EmployeeAddressContext(
        Long employeeId,
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber
) {
}
