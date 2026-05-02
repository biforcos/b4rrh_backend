package com.b4rrhh.employee.photo.domain;

public class EmployeeNotFoundForPhotoException extends RuntimeException {

    public EmployeeNotFoundForPhotoException(
            String ruleSystemCode, String employeeTypeCode, String employeeNumber) {
        super("Employee not found: " + ruleSystemCode + "/" + employeeTypeCode + "/" + employeeNumber);
    }
}
