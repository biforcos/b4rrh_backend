package com.b4rrhh.rulesystem.application.usecase;

import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SearchRuleEntityByBusinessKeyService implements SearchRuleEntityByBusinessKeyUseCase {

    private final RuleEntityRepository ruleEntityRepository;

    public SearchRuleEntityByBusinessKeyService(RuleEntityRepository ruleEntityRepository) {
        this.ruleEntityRepository = ruleEntityRepository;
    }

    @Override
    public Optional<RuleEntity> search(String ruleSystemCode, String ruleEntityTypeCode, String code) {
        return ruleEntityRepository.findByBusinessKey(
                ruleSystemCode.trim().toUpperCase(),
                ruleEntityTypeCode.trim().toUpperCase(),
                code.trim().toUpperCase()
        );
    }
}
