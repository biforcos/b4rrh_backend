package com.b4rrhh.employee.contract.application.port;

public record EmployeeContractContext(
        Long employeeId,
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber
) {
}
