package com.b4rrhh.rulesystem.companyprofile.domain.exception;

public class CompanyProfileCountryInvalidException extends RuntimeException {

    public CompanyProfileCountryInvalidException(String ruleSystemCode, String countryCode) {
        super("countryCode " + countryCode + " is invalid for ruleSystemCode " + ruleSystemCode);
    }
}