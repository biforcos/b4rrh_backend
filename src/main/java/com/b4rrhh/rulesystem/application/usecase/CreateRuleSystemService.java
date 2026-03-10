package com.b4rrhh.rulesystem.application.usecase;

import com.b4rrhh.rulesystem.domain.model.RuleSystem;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateRuleSystemService implements CreateRuleSystemUseCase {

    private final RuleSystemRepository ruleSystemRepository;

    public CreateRuleSystemService(RuleSystemRepository ruleSystemRepository) {
        this.ruleSystemRepository = ruleSystemRepository;
    }

    @Override
    public RuleSystem create(CreateRuleSystemCommand command) {
        String normalizedCode = command.code().trim().toUpperCase();
        String normalizedCountryCode = command.countryCode().trim().toUpperCase();

        ruleSystemRepository.findByCode(normalizedCode).ifPresent(existing -> {
            throw new IllegalArgumentException("Rule system already exists with code: " + normalizedCode);
        });

        RuleSystem ruleSystem = new RuleSystem(
                null,
                normalizedCode,
                command.name().trim(),
                normalizedCountryCode,
                true,
                null,
                null
        );

        return ruleSystemRepository.save(ruleSystem);
    }
}