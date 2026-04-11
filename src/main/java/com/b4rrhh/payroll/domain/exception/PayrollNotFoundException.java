package com.b4rrhh.payroll.domain.exception;

public class PayrollNotFoundException extends RuntimeException {

    public PayrollNotFoundException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            String payrollPeriodCode,
            String payrollTypeCode,
            Integer presenceNumber
    ) {
        super("Payroll not found with business key: "
                + ruleSystemCode + "/"
                + employeeTypeCode + "/"
                + employeeNumber + "/"
                + payrollPeriodCode + "/"
                + payrollTypeCode + "/"
                + presenceNumber);
    }
}