package com.b4rrhh.rulesystem.application.usecase;

import com.b4rrhh.rulesystem.domain.model.RuleEntityType;
import com.b4rrhh.rulesystem.domain.port.RuleEntityTypeRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateRuleEntityTypeService implements CreateRuleEntityTypeUseCase {

    private final RuleEntityTypeRepository ruleEntityTypeRepository;

    public CreateRuleEntityTypeService(RuleEntityTypeRepository ruleEntityTypeRepository) {
        this.ruleEntityTypeRepository = ruleEntityTypeRepository;
    }

    @Override
    public RuleEntityType create(CreateRuleEntityTypeCommand command) {
        String normalizedCode = command.code().trim().toUpperCase();

        ruleEntityTypeRepository.findByCode(normalizedCode).ifPresent(existing -> {
            throw new IllegalArgumentException("Rule entity type already exists with code: " + normalizedCode);
        });

        RuleEntityType ruleEntityType = new RuleEntityType(
                null,
                normalizedCode,
                command.name().trim(),
                true,
                null,
                null
        );

        return ruleEntityTypeRepository.save(ruleEntityType);
    }
}
