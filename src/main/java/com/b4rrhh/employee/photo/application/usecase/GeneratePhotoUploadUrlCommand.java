package com.b4rrhh.employee.photo.application.usecase;

public record GeneratePhotoUploadUrlCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber
) {
}
