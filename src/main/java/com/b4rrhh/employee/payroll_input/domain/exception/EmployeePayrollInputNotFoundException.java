package com.b4rrhh.employee.payroll_input.domain.exception;

public class EmployeePayrollInputNotFoundException extends RuntimeException {

    public EmployeePayrollInputNotFoundException(String conceptCode, int period) {
        super("No existe un input de nómina para el concepto '" + conceptCode +
                "' en el período " + period);
    }
}
