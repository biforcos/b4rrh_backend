package com.b4rrhh.payroll.domain.exception;

public class PayrollEmployeePresenceNotFoundException extends RuntimeException {

    public PayrollEmployeePresenceNotFoundException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            Integer presenceNumber
    ) {
        super("Employee presence not found with business key: "
                + ruleSystemCode + "/"
                + employeeTypeCode + "/"
                + employeeNumber + "/"
                + presenceNumber);
    }
}