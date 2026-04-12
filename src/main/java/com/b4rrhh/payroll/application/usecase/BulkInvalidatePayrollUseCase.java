package com.b4rrhh.payroll.application.usecase;

public interface BulkInvalidatePayrollUseCase {
    BulkInvalidatePayrollResult invalidateBulk(BulkInvalidatePayrollCommand command);
}
