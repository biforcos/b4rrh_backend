package com.b4rrhh.rulesystem.companyprofile.application.service;

import com.b4rrhh.rulesystem.companyprofile.application.usecase.CompanyProfileRuleEntityTypeCodes;
import com.b4rrhh.rulesystem.companyprofile.domain.exception.CompanyProfileCountryInvalidException;
import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class CompanyProfileCatalogValidator {

    private final RuleEntityRepository ruleEntityRepository;

    public CompanyProfileCatalogValidator(RuleEntityRepository ruleEntityRepository) {
        this.ruleEntityRepository = ruleEntityRepository;
    }

    public void validateCountryCode(String ruleSystemCode, String countryCode, LocalDate referenceDate) {
        if (countryCode == null) {
            return;
        }

        RuleEntity ruleEntity = ruleEntityRepository
                .findApplicableByBusinessKey(ruleSystemCode, CompanyProfileRuleEntityTypeCodes.COUNTRY, countryCode, referenceDate)
                .orElseThrow(() -> new CompanyProfileCountryInvalidException(ruleSystemCode, countryCode));

        if (!ruleEntity.isActive() || !isDateApplicable(ruleEntity, referenceDate)) {
            throw new CompanyProfileCountryInvalidException(ruleSystemCode, countryCode);
        }
    }

    private boolean isDateApplicable(RuleEntity ruleEntity, LocalDate referenceDate) {
        if (referenceDate == null) {
            return true;
        }

        boolean startsBeforeOrOnDate = !referenceDate.isBefore(ruleEntity.getStartDate());
        boolean endsAfterOrOnDate = ruleEntity.getEndDate() == null || !referenceDate.isAfter(ruleEntity.getEndDate());

        return startsBeforeOrOnDate && endsAfterOrOnDate;
    }
}