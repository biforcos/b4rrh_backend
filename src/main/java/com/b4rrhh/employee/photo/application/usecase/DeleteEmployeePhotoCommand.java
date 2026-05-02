package com.b4rrhh.employee.photo.application.usecase;

public record DeleteEmployeePhotoCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber
) {
}
