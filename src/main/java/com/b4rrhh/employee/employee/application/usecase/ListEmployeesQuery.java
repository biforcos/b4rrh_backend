package com.b4rrhh.employee.employee.application.usecase;

public record ListEmployeesQuery(
        String q,
        String ruleSystemCode,
        String employeeTypeCode,
        String status,
        Integer page,
        Integer size
) {
}