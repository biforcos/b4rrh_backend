package com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception;

public class AgreementCategoryProfileCategoryNotFoundException extends RuntimeException {
    public AgreementCategoryProfileCategoryNotFoundException(String ruleSystemCode, String categoryCode) {
        super("Agreement category not found: " + ruleSystemCode + "/" + categoryCode);
    }
}
