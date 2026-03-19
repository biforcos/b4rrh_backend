package com.b4rrhh.employee.labor_classification.application.service;

import com.b4rrhh.employee.labor_classification.application.usecase.LaborClassificationRuleEntityTypeCodes;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationAgreementInvalidException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationCategoryInvalidException;
import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class LaborClassificationCatalogValidator {

    private final RuleEntityRepository ruleEntityRepository;

    public LaborClassificationCatalogValidator(RuleEntityRepository ruleEntityRepository) {
        this.ruleEntityRepository = ruleEntityRepository;
    }

    public String normalizeRequiredCode(String fieldName, String value) {
        if (value == null || value.trim().isEmpty()) {
            if ("agreementCode".equals(fieldName)) {
                throw new LaborClassificationAgreementInvalidException(String.valueOf(value));
            }
            if ("agreementCategoryCode".equals(fieldName)) {
                throw new LaborClassificationCategoryInvalidException(String.valueOf(value));
            }
            throw new IllegalArgumentException(fieldName + " is required");
        }

        return value.trim().toUpperCase();
    }

    public void validateAgreementCode(String ruleSystemCode, String agreementCode, LocalDate referenceDate) {
        RuleEntity ruleEntity = ruleEntityRepository
                .findByBusinessKey(ruleSystemCode, LaborClassificationRuleEntityTypeCodes.AGREEMENT, agreementCode)
                .orElseThrow(() -> new LaborClassificationAgreementInvalidException(agreementCode));

        if (!ruleEntity.isActive() || !isDateApplicable(ruleEntity, referenceDate)) {
            throw new LaborClassificationAgreementInvalidException(agreementCode);
        }
    }

    public void validateAgreementCategoryCode(
            String ruleSystemCode,
            String agreementCategoryCode,
            LocalDate referenceDate
    ) {
        RuleEntity ruleEntity = ruleEntityRepository
                .findByBusinessKey(
                        ruleSystemCode,
                        LaborClassificationRuleEntityTypeCodes.AGREEMENT_CATEGORY,
                        agreementCategoryCode
                )
                .orElseThrow(() -> new LaborClassificationCategoryInvalidException(agreementCategoryCode));

        if (!ruleEntity.isActive() || !isDateApplicable(ruleEntity, referenceDate)) {
            throw new LaborClassificationCategoryInvalidException(agreementCategoryCode);
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
