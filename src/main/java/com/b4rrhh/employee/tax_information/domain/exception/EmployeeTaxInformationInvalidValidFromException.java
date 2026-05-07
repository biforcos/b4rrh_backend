package com.b4rrhh.employee.tax_information.domain.exception;

import java.time.LocalDate;

public class EmployeeTaxInformationInvalidValidFromException extends RuntimeException {
    public EmployeeTaxInformationInvalidValidFromException(LocalDate validFrom) {
        super("valid_from " + validFrom + " must be the first day of a month or a presence start date");
    }
}
