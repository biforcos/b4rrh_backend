package com.b4rrhh.employee.tax_information.domain.exception;

import java.time.LocalDate;

public class EmployeeTaxInformationAlreadyExistsException extends RuntimeException {
    public EmployeeTaxInformationAlreadyExistsException(Long employeeId, LocalDate validFrom) {
        super("Tax information already exists for employee " + employeeId + " valid from " + validFrom);
    }
}
