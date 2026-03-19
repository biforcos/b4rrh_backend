package com.b4rrhh.employee.labor_classification.domain.exception;

public class LaborClassificationAgreementInvalidException extends RuntimeException {

    public LaborClassificationAgreementInvalidException(String agreementCode) {
        super("Invalid agreementCode: " + agreementCode);
    }
}
