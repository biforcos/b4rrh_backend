package com.b4rrhh.rulesystem.application.usecase;

import com.b4rrhh.rulesystem.domain.model.RuleSystem;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListRuleSystemsService implements ListRuleSystemsUseCase {

    private final RuleSystemRepository ruleSystemRepository;

    public ListRuleSystemsService(RuleSystemRepository ruleSystemRepository) {
        this.ruleSystemRepository = ruleSystemRepository;
    }

    @Override
    public List<RuleSystem> listAll() {
        return ruleSystemRepository.findAll();
    }
}