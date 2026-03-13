package com.b4rrhh.employee.contact.application.service;

import com.b4rrhh.employee.contact.application.usecase.ContactRuleEntityTypeCodes;
import com.b4rrhh.employee.contact.domain.exception.ContactCatalogValueInvalidException;
import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ContactCatalogValidator {

    private final RuleEntityRepository ruleEntityRepository;

    public ContactCatalogValidator(RuleEntityRepository ruleEntityRepository) {
        this.ruleEntityRepository = ruleEntityRepository;
    }

    public String normalizeRequiredCode(String fieldName, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ContactCatalogValueInvalidException(fieldName, String.valueOf(value));
        }

        return value.trim().toUpperCase();
    }

    public void validateContactTypeCode(String ruleSystemCode, String contactTypeCode, LocalDate referenceDate) {
        validateCatalog(
                ruleSystemCode,
                ContactRuleEntityTypeCodes.EMPLOYEE_CONTACT_TYPE,
                contactTypeCode,
                "contactTypeCode",
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
                .orElseThrow(() -> new ContactCatalogValueInvalidException(fieldName, code));

        if (!ruleEntity.isActive() || !isDateApplicable(ruleEntity, referenceDate)) {
            throw new ContactCatalogValueInvalidException(fieldName, code);
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
