package com.b4rrhh.employee.tax_information.domain.exception;

import java.time.LocalDate;

public class EmployeeTaxInformationNotFoundException extends RuntimeException {
    public EmployeeTaxInformationNotFoundException(Long employeeId, LocalDate validFrom) {
        super("Tax information not found for employee " + employeeId + " valid from " + validFrom);
    }
}
