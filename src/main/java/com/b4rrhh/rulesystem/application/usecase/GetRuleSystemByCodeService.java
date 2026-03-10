package com.b4rrhh.rulesystem.application.usecase;

import com.b4rrhh.rulesystem.domain.model.RuleSystem;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetRuleSystemByCodeService implements GetRuleSystemByCodeUseCase {

    private final RuleSystemRepository ruleSystemRepository;

    public GetRuleSystemByCodeService(RuleSystemRepository ruleSystemRepository) {
        this.ruleSystemRepository = ruleSystemRepository;
    }

    @Override
    public Optional<RuleSystem> getByCode(String code) {
        return ruleSystemRepository.findByCode(code.trim().toUpperCase());
    }
}