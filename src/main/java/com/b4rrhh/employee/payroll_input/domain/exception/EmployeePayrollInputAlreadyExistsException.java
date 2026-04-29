package com.b4rrhh.employee.payroll_input.domain.exception;

public class EmployeePayrollInputAlreadyExistsException extends RuntimeException {

    public EmployeePayrollInputAlreadyExistsException(String conceptCode, int period) {
        super("Ya existe un input de nómina para el concepto '" + conceptCode +
                "' en el período " + period);
    }
}
