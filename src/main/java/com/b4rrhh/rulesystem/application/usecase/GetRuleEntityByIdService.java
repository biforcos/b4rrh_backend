package com.b4rrhh.rulesystem.application.usecase;

import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetRuleEntityByIdService implements GetRuleEntityByIdUseCase {

    private final RuleEntityRepository ruleEntityRepository;

    public GetRuleEntityByIdService(RuleEntityRepository ruleEntityRepository) {
        this.ruleEntityRepository = ruleEntityRepository;
    }

    @Override
    public Optional<RuleEntity> getById(Long id) {
        return ruleEntityRepository.findById(id);
    }
}
