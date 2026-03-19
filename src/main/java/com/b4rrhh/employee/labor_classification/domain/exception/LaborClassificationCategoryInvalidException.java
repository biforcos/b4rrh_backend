package com.b4rrhh.employee.labor_classification.domain.exception;

public class LaborClassificationCategoryInvalidException extends RuntimeException {

    public LaborClassificationCategoryInvalidException(String agreementCategoryCode) {
        super("Invalid agreementCategoryCode: " + agreementCategoryCode);
    }
}
