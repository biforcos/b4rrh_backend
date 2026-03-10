package com.b4rrhh.rulesystem.application.usecase;

import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import com.b4rrhh.rulesystem.domain.port.RuleEntityTypeRepository;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateRuleEntityService implements CreateRuleEntityUseCase {

    private final RuleEntityRepository ruleEntityRepository;
    private final RuleSystemRepository ruleSystemRepository;
    private final RuleEntityTypeRepository ruleEntityTypeRepository;

    public CreateRuleEntityService(
            RuleEntityRepository ruleEntityRepository,
            RuleSystemRepository ruleSystemRepository,
            RuleEntityTypeRepository ruleEntityTypeRepository
    ) {
        this.ruleEntityRepository = ruleEntityRepository;
        this.ruleSystemRepository = ruleSystemRepository;
        this.ruleEntityTypeRepository = ruleEntityTypeRepository;
    }

    @Override
    public RuleEntity create(CreateRuleEntityCommand command) {
        String normalizedRuleSystemCode = command.ruleSystemCode().trim().toUpperCase();
        String normalizedRuleEntityTypeCode = command.ruleEntityTypeCode().trim().toUpperCase();
        String normalizedCode = command.code().trim().toUpperCase();

        ruleSystemRepository.findByCode(normalizedRuleSystemCode).orElseThrow(
                () -> new IllegalArgumentException("Rule system not found with code: " + normalizedRuleSystemCode)
        );

        ruleEntityTypeRepository.findByCode(normalizedRuleEntityTypeCode).orElseThrow(
                () -> new IllegalArgumentException("Rule entity type not found with code: " + normalizedRuleEntityTypeCode)
        );

        ruleEntityRepository.findByBusinessKey(normalizedRuleSystemCode, normalizedRuleEntityTypeCode, normalizedCode)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Rule entity already exists with business key: "
                                    + normalizedRuleSystemCode + "/" + normalizedRuleEntityTypeCode + "/" + normalizedCode
                    );
                });

        RuleEntity ruleEntity = new RuleEntity(
                null,
                normalizedRuleSystemCode,
                normalizedRuleEntityTypeCode,
                normalizedCode,
                command.name().trim(),
                normalizeDescription(command.description()),
                true,
                command.startDate(),
                command.endDate(),
                null,
                null
        );

        return ruleEntityRepository.save(ruleEntity);
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }

        String normalized = description.trim();
        if (normalized.isEmpty()) {
            return null;
        }

        return normalized;
    }
}
