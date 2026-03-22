package com.b4rrhh.employee.presence.infrastructure.persistence;

import com.b4rrhh.employee.presence.application.port.PresenceCatalogReadPort;
import com.b4rrhh.employee.presence.application.usecase.PresenceRuleEntityTypeCodes;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PresenceCatalogReadAdapter implements PresenceCatalogReadPort {

    private final RuleEntityRepository ruleEntityRepository;

    public PresenceCatalogReadAdapter(RuleEntityRepository ruleEntityRepository) {
        this.ruleEntityRepository = ruleEntityRepository;
    }

    @Override
    public Optional<String> findCompanyName(String ruleSystemCode, String companyCode) {
        String normalizedRuleSystemCode = normalizeToUppercase(ruleSystemCode);
        String normalizedCompanyCode = normalizeToUppercase(companyCode);
        if (normalizedRuleSystemCode == null || normalizedCompanyCode == null) {
            return Optional.empty();
        }

        return findCatalogName(
                normalizedRuleSystemCode,
                PresenceRuleEntityTypeCodes.EMPLOYEE_PRESENCE_COMPANY,
                normalizedCompanyCode
        );
    }

    @Override
    public Optional<String> findEntryReasonName(String ruleSystemCode, String entryReasonCode) {
        String normalizedRuleSystemCode = normalizeToUppercase(ruleSystemCode);
        String normalizedEntryReasonCode = normalizeToUppercase(entryReasonCode);
        if (normalizedRuleSystemCode == null || normalizedEntryReasonCode == null) {
            return Optional.empty();
        }

        return findCatalogName(
                normalizedRuleSystemCode,
                PresenceRuleEntityTypeCodes.EMPLOYEE_PRESENCE_ENTRY_REASON,
                normalizedEntryReasonCode
        );
    }

    @Override
    public Optional<String> findExitReasonName(String ruleSystemCode, String exitReasonCode) {
        String normalizedRuleSystemCode = normalizeToUppercase(ruleSystemCode);
        String normalizedExitReasonCode = normalizeToUppercase(exitReasonCode);
        if (normalizedRuleSystemCode == null || normalizedExitReasonCode == null) {
            return Optional.empty();
        }

        return findCatalogName(
                normalizedRuleSystemCode,
                PresenceRuleEntityTypeCodes.EMPLOYEE_PRESENCE_EXIT_REASON,
                normalizedExitReasonCode
        );
    }

    private Optional<String> findCatalogName(String ruleSystemCode, String ruleEntityTypeCode, String code) {
        return ruleEntityRepository
                .findByBusinessKey(ruleSystemCode, ruleEntityTypeCode, code)
                .map(entity -> entity.getName() == null ? null : entity.getName().trim())
                .filter(name -> name != null && !name.isEmpty());
    }

    private String normalizeToUppercase(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim().toUpperCase();
    }
}
