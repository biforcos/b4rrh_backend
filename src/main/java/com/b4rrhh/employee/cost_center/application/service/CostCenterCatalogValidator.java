package com.b4rrhh.employee.cost_center.application.service;

import com.b4rrhh.employee.cost_center.application.usecase.CostCenterRuleEntityTypeCodes;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterCatalogValueInvalidException;
import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class CostCenterCatalogValidator {

    private final RuleEntityRepository ruleEntityRepository;

    public CostCenterCatalogValidator(RuleEntityRepository ruleEntityRepository) {
        this.ruleEntityRepository = ruleEntityRepository;
    }

    public String normalizeRequiredCode(String fieldName, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new CostCenterCatalogValueInvalidException(fieldName, String.valueOf(value));
        }

        return value.trim().toUpperCase();
    }

    public void validateCostCenterCode(String ruleSystemCode, String costCenterCode, LocalDate referenceDate) {
        validateCatalog(
                ruleSystemCode,
                CostCenterRuleEntityTypeCodes.COST_CENTER,
                costCenterCode,
                "costCenterCode",
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
                .orElseThrow(() -> new CostCenterCatalogValueInvalidException(fieldName, code));

        if (!ruleEntity.isActive() || !isDateApplicable(ruleEntity, referenceDate)) {
            throw new CostCenterCatalogValueInvalidException(fieldName, code);
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
