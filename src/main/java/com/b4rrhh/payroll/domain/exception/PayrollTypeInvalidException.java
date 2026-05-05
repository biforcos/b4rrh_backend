package com.b4rrhh.payroll.domain.exception;

public class PayrollTypeInvalidException extends RuntimeException {

    public PayrollTypeInvalidException(String code) {
        super("Invalid payrollTypeCode: '" + code + "'");
    }
}
