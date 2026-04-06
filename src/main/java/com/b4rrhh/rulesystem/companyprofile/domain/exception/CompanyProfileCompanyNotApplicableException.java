package com.b4rrhh.rulesystem.companyprofile.domain.exception;

import java.time.LocalDate;

public class CompanyProfileCompanyNotApplicableException extends RuntimeException {

    public CompanyProfileCompanyNotApplicableException(String ruleSystemCode, String companyCode, LocalDate referenceDate) {
        super(
                "Company is not active or applicable on "
                        + referenceDate
                        + " for ruleSystemCode "
                        + ruleSystemCode
                        + " and companyCode "
                        + companyCode
        );
    }
}