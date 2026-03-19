package com.b4rrhh.employee.labor_classification.domain.exception;

import java.time.LocalDate;

public class LaborClassificationAlreadyClosedException extends RuntimeException {

    public LaborClassificationAlreadyClosedException(LocalDate startDate) {
        super("Labor classification is already closed for startDate=" + startDate);
    }
}
