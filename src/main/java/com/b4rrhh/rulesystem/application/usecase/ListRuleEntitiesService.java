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
        return ruleEntityRepository.findByFilters(
                normalizeOptionalCode(query.ruleSystemCode()),
                normalizeOptionalCode(query.ruleEntityTypeCode()),
                normalizeOptionalCode(query.code()),
                query.active()
        );
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
