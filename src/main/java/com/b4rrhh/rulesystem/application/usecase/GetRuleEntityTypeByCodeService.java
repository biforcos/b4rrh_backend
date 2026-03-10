package com.b4rrhh.rulesystem.application.usecase;

import com.b4rrhh.rulesystem.domain.model.RuleEntityType;
import com.b4rrhh.rulesystem.domain.port.RuleEntityTypeRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetRuleEntityTypeByCodeService implements GetRuleEntityTypeByCodeUseCase {

    private final RuleEntityTypeRepository ruleEntityTypeRepository;

    public GetRuleEntityTypeByCodeService(RuleEntityTypeRepository ruleEntityTypeRepository) {
        this.ruleEntityTypeRepository = ruleEntityTypeRepository;
    }

    @Override
    public Optional<RuleEntityType> getByCode(String code) {
        return ruleEntityTypeRepository.findByCode(code.trim().toUpperCase());
    }
}
