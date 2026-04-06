package com.b4rrhh.rulesystem.companyprofile.application.service;

import com.b4rrhh.rulesystem.companyprofile.application.usecase.CompanyProfileRuleEntityTypeCodes;
import com.b4rrhh.rulesystem.companyprofile.domain.exception.CompanyProfileCompanyNotApplicableException;
import com.b4rrhh.rulesystem.companyprofile.domain.exception.CompanyProfileCompanyNotFoundException;
import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class CompanyProfileCompanyResolver {

    private final RuleEntityRepository ruleEntityRepository;

    public CompanyProfileCompanyResolver(RuleEntityRepository ruleEntityRepository) {
        this.ruleEntityRepository = ruleEntityRepository;
    }

    // Company profile APIs resolve COMPANY against the occurrence applicable on the server current date.
    // Non-applicable or closed occurrences remain outside this API surface and are treated as unreachable.
    public RuleEntity resolveApplicableToday(String ruleSystemCode, String companyCode) {
        LocalDate referenceDate = LocalDate.now();

        return ruleEntityRepository
                .findApplicableByBusinessKey(
                        ruleSystemCode,
                        CompanyProfileRuleEntityTypeCodes.COMPANY,
                        companyCode,
                        referenceDate
                )
                .orElseGet(() -> {
                    boolean companyExists = !ruleEntityRepository
                            .findByFilters(ruleSystemCode, CompanyProfileRuleEntityTypeCodes.COMPANY, companyCode, null, null)
                            .isEmpty();

                    if (companyExists) {
                        throw new CompanyProfileCompanyNotApplicableException(ruleSystemCode, companyCode, referenceDate);
                    }

                    throw new CompanyProfileCompanyNotFoundException(ruleSystemCode, companyCode);
                });
    }
}