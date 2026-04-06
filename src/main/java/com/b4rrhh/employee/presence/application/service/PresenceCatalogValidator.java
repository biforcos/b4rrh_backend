package com.b4rrhh.employee.presence.application.service;

import com.b4rrhh.employee.presence.application.usecase.PresenceRuleEntityTypeCodes;
import com.b4rrhh.employee.presence.domain.exception.PresenceCatalogValueInvalidException;
import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PresenceCatalogValidator {

    private final RuleEntityRepository ruleEntityRepository;

    public PresenceCatalogValidator(RuleEntityRepository ruleEntityRepository) {
        this.ruleEntityRepository = ruleEntityRepository;
    }

    public String normalizeRequiredCode(String fieldName, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new PresenceCatalogValueInvalidException(fieldName, String.valueOf(value));
        }

        return value.trim().toUpperCase();
    }

    public String normalizeOptionalCode(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim().toUpperCase();
    }

    public void validateCompanyCode(String ruleSystemCode, String companyCode, LocalDate referenceDate) {
        validateCatalog(ruleSystemCode, PresenceRuleEntityTypeCodes.COMPANY, companyCode, "companyCode", referenceDate);
    }

    public void validateEntryReasonCode(String ruleSystemCode, String entryReasonCode, LocalDate referenceDate) {
        validateCatalog(
                ruleSystemCode,
                PresenceRuleEntityTypeCodes.EMPLOYEE_PRESENCE_ENTRY_REASON,
                entryReasonCode,
                "entryReasonCode",
                referenceDate
        );
    }

    public void validateExitReasonCode(String ruleSystemCode, String exitReasonCode, LocalDate referenceDate) {
        validateCatalog(
                ruleSystemCode,
                PresenceRuleEntityTypeCodes.EMPLOYEE_PRESENCE_EXIT_REASON,
                exitReasonCode,
                "exitReasonCode",
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
                .orElseThrow(() -> new PresenceCatalogValueInvalidException(fieldName, code));

        if (!ruleEntity.isActive() || !isDateApplicable(ruleEntity, referenceDate)) {
            throw new PresenceCatalogValueInvalidException(fieldName, code);
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
