package com.b4rrhh.rulesystem.application.usecase;

import com.b4rrhh.rulesystem.domain.model.RuleEntityType;
import com.b4rrhh.rulesystem.domain.port.RuleEntityTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListRuleEntityTypesService implements ListRuleEntityTypesUseCase {

    private final RuleEntityTypeRepository ruleEntityTypeRepository;

    public ListRuleEntityTypesService(RuleEntityTypeRepository ruleEntityTypeRepository) {
        this.ruleEntityTypeRepository = ruleEntityTypeRepository;
    }

    @Override
    public List<RuleEntityType> listAll() {
        return ruleEntityTypeRepository.findAll();
    }
}
