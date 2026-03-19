package com.b4rrhh.employee.labor_classification.application.port;

public record EmployeeLaborClassificationContext(
        Long employeeId,
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber
) {
}
