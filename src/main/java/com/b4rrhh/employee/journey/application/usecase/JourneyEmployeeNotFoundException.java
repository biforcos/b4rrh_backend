package com.b4rrhh.employee.journey.application.usecase;

public class JourneyEmployeeNotFoundException extends RuntimeException {

    public JourneyEmployeeNotFoundException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        super("Employee not found for journey by business key: ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber);
    }
}
