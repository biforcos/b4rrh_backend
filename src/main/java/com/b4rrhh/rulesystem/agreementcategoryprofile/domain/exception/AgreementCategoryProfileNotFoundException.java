package com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception;

public class AgreementCategoryProfileNotFoundException extends RuntimeException {
    public AgreementCategoryProfileNotFoundException(String ruleSystemCode, String categoryCode) {
        super("Agreement category profile not found for: " + ruleSystemCode + "/" + categoryCode);
    }
}
