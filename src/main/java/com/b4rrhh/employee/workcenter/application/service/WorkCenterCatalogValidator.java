package com.b4rrhh.employee.workcenter.application.service;

import com.b4rrhh.employee.workcenter.application.usecase.WorkCenterRuleEntityTypeCodes;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterCatalogValueInvalidException;
import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class WorkCenterCatalogValidator {

    private final RuleEntityRepository ruleEntityRepository;

    public WorkCenterCatalogValidator(RuleEntityRepository ruleEntityRepository) {
        this.ruleEntityRepository = ruleEntityRepository;
    }

    public String normalizeRequiredCode(String fieldName, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new WorkCenterCatalogValueInvalidException(fieldName, String.valueOf(value));
        }

        return value.trim().toUpperCase();
    }

    public void validateWorkCenterCode(String ruleSystemCode, String workCenterCode, LocalDate referenceDate) {
        validateCatalog(
                ruleSystemCode,
                WorkCenterRuleEntityTypeCodes.WORK_CENTER,
                workCenterCode,
                "workCenterCode",
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
                .orElseThrow(() -> new WorkCenterCatalogValueInvalidException(fieldName, code));

        if (!ruleEntity.isActive() || !isDateApplicable(ruleEntity, referenceDate)) {
            throw new WorkCenterCatalogValueInvalidException(fieldName, code);
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