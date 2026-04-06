package com.b4rrhh.rulesystem.companyprofile.domain.exception;

public class CompanyProfileNotFoundException extends RuntimeException {

    public CompanyProfileNotFoundException(String ruleSystemCode, String companyCode) {
        super("Company profile not found for ruleSystemCode " + ruleSystemCode + " and companyCode " + companyCode);
    }
}