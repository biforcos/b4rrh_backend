package com.b4rrhh.rulesystem.company.domain.exception;

public class CompanyAlreadyExistsException extends RuntimeException {

    public CompanyAlreadyExistsException(String ruleSystemCode, String companyCode) {
        super("Company already exists for ruleSystemCode " + ruleSystemCode + " and companyCode " + companyCode);
    }
}
