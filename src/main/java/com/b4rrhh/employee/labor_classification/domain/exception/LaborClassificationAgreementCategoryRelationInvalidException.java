package com.b4rrhh.employee.labor_classification.domain.exception;

import java.time.LocalDate;

public class LaborClassificationAgreementCategoryRelationInvalidException extends RuntimeException {

    public LaborClassificationAgreementCategoryRelationInvalidException(
            String ruleSystemCode,
            String agreementCode,
            String agreementCategoryCode,
            LocalDate referenceDate
    ) {
        super("Invalid agreement-category relation for ruleSystemCode="
                + ruleSystemCode
                + ", agreementCode="
                + agreementCode
                + ", agreementCategoryCode="
                + agreementCategoryCode
                + ", referenceDate="
                + referenceDate);
    }
}
