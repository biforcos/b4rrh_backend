package com.b4rrhh.employee.employee.domain.exception;

public class EmployeeTypeInvalidException extends RuntimeException {
    public EmployeeTypeInvalidException(String code) {
        super("Invalid employeeTypeCode: '" + code + "'");
    }
}
