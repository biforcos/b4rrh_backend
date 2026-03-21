package com.b4rrhh.employee.employee.domain.exception;

public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(String ruleSystemCode, String employeeTypeCode, String employeeNumber) {
        super("Employee not found with business key: "
                + ruleSystemCode + "/" + employeeTypeCode + "/" + employeeNumber);
    }
}