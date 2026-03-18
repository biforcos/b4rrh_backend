package com.b4rrhh.employee.cost_center.domain.exception;

public class CostCenterEmployeeNotFoundException extends RuntimeException {

    public CostCenterEmployeeNotFoundException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        super("Employee not found for ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber);
    }
}
