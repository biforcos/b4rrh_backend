package com.b4rrhh.employee.identifier.application.service;

import com.b4rrhh.employee.identifier.application.usecase.IdentifierRuleEntityTypeCodes;
import com.b4rrhh.employee.identifier.domain.exception.IdentifierCatalogValueInvalidException;
import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class IdentifierCatalogValidator {

    private final RuleEntityRepository ruleEntityRepository;

    public IdentifierCatalogValidator(RuleEntityRepository ruleEntityRepository) {
        this.ruleEntityRepository = ruleEntityRepository;
    }

    public String normalizeRequiredCode(String fieldName, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IdentifierCatalogValueInvalidException(fieldName, String.valueOf(value));
        }

        return value.trim().toUpperCase();
    }

    public String normalizeOptionalCode(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim().toUpperCase();
    }

    public void validateIdentifierTypeCode(String ruleSystemCode, String identifierTypeCode, LocalDate referenceDate) {
        validateCatalog(
                ruleSystemCode,
                IdentifierRuleEntityTypeCodes.EMPLOYEE_IDENTIFIER_TYPE,
                identifierTypeCode,
                "identifierTypeCode",
                referenceDate
        );
    }

    public void validateCountryCode(String ruleSystemCode, String countryCode, LocalDate referenceDate) {
        validateCatalog(
                ruleSystemCode,
                IdentifierRuleEntityTypeCodes.COUNTRY,
                countryCode,
                "issuingCountryCode",
                referenceDate
        );
    }

    private void validateCatalog(
            String ruleSystemCode,
            String ruleEntityTypeCode,
            String code,
            String fieldName,
            LocalDate referenceDate
    ) {
        RuleEntity ruleEntity = ruleEntityRepository
                .findByBusinessKey(ruleSystemCode, ruleEntityTypeCode, code)
                .orElseThrow(() -> new IdentifierCatalogValueInvalidException(fieldName, code));

        if (!ruleEntity.isActive() || !isDateApplicable(ruleEntity, referenceDate)) {
            throw new IdentifierCatalogValueInvalidException(fieldName, code);
        }
    }

    // Inclusive temporal semantics: startDate <= referenceDate <= endDate (when endDate exists).
    private boolean isDateApplicable(RuleEntity ruleEntity, LocalDate referenceDate) {
        if (referenceDate == null) {
            return true;
        }

        boolean startsBeforeOrOnDate = !referenceDate.isBefore(ruleEntity.getStartDate());
        boolean endsAfterOrOnDate = ruleEntity.getEndDate() == null || !referenceDate.isAfter(ruleEntity.getEndDate());

        return startsBeforeOrOnDate && endsAfterOrOnDate;
    }
}
