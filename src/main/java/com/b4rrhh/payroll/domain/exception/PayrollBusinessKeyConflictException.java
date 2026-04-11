package com.b4rrhh.payroll.domain.exception;

public class PayrollBusinessKeyConflictException extends RuntimeException {

    public PayrollBusinessKeyConflictException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            String payrollPeriodCode,
            String payrollTypeCode,
            Integer presenceNumber
    ) {
        super("Payroll already exists for business key: "
                + ruleSystemCode + "/"
                + employeeTypeCode + "/"
                + employeeNumber + "/"
                + payrollPeriodCode + "/"
                + payrollTypeCode + "/"
                + presenceNumber);
    }
}