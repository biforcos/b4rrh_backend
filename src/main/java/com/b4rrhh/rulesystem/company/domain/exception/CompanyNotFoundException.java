package com.b4rrhh.rulesystem.company.domain.exception;

public class CompanyNotFoundException extends RuntimeException {

    public CompanyNotFoundException(String ruleSystemCode, String companyCode) {
        super("Company not found for ruleSystemCode " + ruleSystemCode + " and companyCode " + companyCode);
    }
}
