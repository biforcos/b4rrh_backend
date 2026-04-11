package com.b4rrhh.payroll.application.usecase;

import com.b4rrhh.payroll.domain.model.Payroll;

public interface ValidatePayrollUseCase {

    Payroll validate(ValidatePayrollCommand command);
}