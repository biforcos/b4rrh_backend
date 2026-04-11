package com.b4rrhh.payroll.domain.exception;

import com.b4rrhh.payroll.domain.model.PayrollStatus;

public class PayrollInvalidStateTransitionException extends RuntimeException {

    public PayrollInvalidStateTransitionException(PayrollStatus currentStatus, String targetAction) {
        super("Cannot " + targetAction + " payroll from status " + currentStatus);
    }
}