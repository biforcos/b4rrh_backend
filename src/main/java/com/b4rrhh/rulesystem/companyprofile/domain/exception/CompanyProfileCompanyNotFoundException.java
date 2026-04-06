package com.b4rrhh.rulesystem.companyprofile.domain.exception;

public class CompanyProfileCompanyNotFoundException extends RuntimeException {

    public CompanyProfileCompanyNotFoundException(String ruleSystemCode, String companyCode) {
        super("Company not found for ruleSystemCode " + ruleSystemCode + " and companyCode " + companyCode);
    }
}