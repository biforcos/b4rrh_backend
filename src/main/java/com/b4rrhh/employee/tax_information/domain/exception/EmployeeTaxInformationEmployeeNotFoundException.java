package com.b4rrhh.employee.tax_information.domain.exception;

public class EmployeeTaxInformationEmployeeNotFoundException extends RuntimeException {
    public EmployeeTaxInformationEmployeeNotFoundException(String ruleSystemCode, String employeeTypeCode, String employeeNumber) {
        super("Employee not found: " + ruleSystemCode + "/" + employeeTypeCode + "/" + employeeNumber);
    }
}
