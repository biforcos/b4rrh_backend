package com.b4rrhh.rulesystem.application.usecase;

import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListRuleEntitiesService implements ListRuleEntitiesUseCase {

    private final RuleEntityRepository ruleEntityRepository;

    public ListRuleEntitiesService(RuleEntityRepository ruleEntityRepository) {
        this.ruleEntityRepository = ruleEntityRepository;
    }

    @Override
    public List<RuleEntity> list(ListRuleEntitiesQuery query) {
        String normalizedRuleSystemCode = normalizeOptionalCode(query.ruleSystemCode());
        String normalizedRuleEntityTypeCode = normalizeOptionalCode(query.ruleEntityTypeCode());
        String normalizedCode = normalizeOptionalCode(query.code());

        if (hasCompleteBusinessKey(normalizedRuleSystemCode, normalizedRuleEntityTypeCode, normalizedCode)
                && query.active() == null) {
            return ruleEntityRepository.findByBusinessKey(
                    normalizedRuleSystemCode,
                    normalizedRuleEntityTypeCode,
                    normalizedCode
            ).stream().toList();
        }

        return ruleEntityRepository.findByFilters(
                normalizedRuleSystemCode,
                normalizedRuleEntityTypeCode,
                normalizedCode,
                query.active()
        );
    }

    private boolean hasCompleteBusinessKey(String ruleSystemCode, String ruleEntityTypeCode, String code) {
        return ruleSystemCode != null && ruleEntityTypeCode != null && code != null;
    }

    private String normalizeOptionalCode(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }

        return normalized.toUpperCase();
    }
}
