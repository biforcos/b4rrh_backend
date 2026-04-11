package com.b4rrhh.payroll.domain.exception;

import com.b4rrhh.payroll.domain.model.PayrollStatus;

public class PayrollRecalculationNotAllowedException extends RuntimeException {

    public PayrollRecalculationNotAllowedException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            String payrollPeriodCode,
            String payrollTypeCode,
            Integer presenceNumber,
            PayrollStatus currentStatus
    ) {
        super("Payroll can only be recalculated from NOT_VALID. Current state for business key "
                + ruleSystemCode + "/"
                + employeeTypeCode + "/"
                + employeeNumber + "/"
                + payrollPeriodCode + "/"
                + payrollTypeCode + "/"
                + presenceNumber + " is " + currentStatus);
    }
}