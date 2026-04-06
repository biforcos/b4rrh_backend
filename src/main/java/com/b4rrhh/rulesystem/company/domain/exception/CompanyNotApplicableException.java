package com.b4rrhh.rulesystem.company.domain.exception;

import java.time.LocalDate;

public class CompanyNotApplicableException extends RuntimeException {

    public CompanyNotApplicableException(String ruleSystemCode, String companyCode, LocalDate referenceDate) {
        super(
                "Company exists but is not applicable on referenceDate "
                        + referenceDate
                        + " for ruleSystemCode "
                        + ruleSystemCode
                        + " and companyCode "
                        + companyCode
        );
    }
}
