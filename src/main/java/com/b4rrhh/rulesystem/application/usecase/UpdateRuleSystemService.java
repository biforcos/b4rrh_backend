package com.b4rrhh.rulesystem.application.usecase;

import com.b4rrhh.rulesystem.domain.exception.RuleSystemNotFoundException;
import com.b4rrhh.rulesystem.domain.model.RuleSystem;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateRuleSystemService implements UpdateRuleSystemUseCase {

    private final RuleSystemRepository ruleSystemRepository;

    public UpdateRuleSystemService(RuleSystemRepository ruleSystemRepository) {
        this.ruleSystemRepository = ruleSystemRepository;
    }

    @Override
    @Transactional
    public RuleSystem execute(UpdateRuleSystemCommand command) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(command.ruleSystemCode());
        String normalizedName = normalizeName(command.name());
        String normalizedCountryCode = normalizeCountryCode(command.countryCode());
        boolean normalizedActive = normalizeActive(command.active());

        RuleSystem existing = ruleSystemRepository.findByCode(normalizedRuleSystemCode)
                .orElseThrow(() -> new RuleSystemNotFoundException(normalizedRuleSystemCode));

        // Rule system decisions in V1:
        // - Business key (ruleSystemCode) is immutable and never updated.
        // - countryCode is informational metadata, without behavior-side effects.
        // - active toggles visibility/usage state, no cascading actions are triggered.
        existing.update(normalizedName, normalizedCountryCode, normalizedActive);

        return ruleSystemRepository.save(existing);
    }

    private String normalizeRuleSystemCode(String ruleSystemCode) {
        if (ruleSystemCode == null || ruleSystemCode.trim().isEmpty()) {
            throw new IllegalArgumentException("ruleSystemCode is required");
        }

        String normalized = ruleSystemCode.trim().toUpperCase();
        if (normalized.length() > 5) {
            throw new IllegalArgumentException("ruleSystemCode exceeds max length 5");
        }

        return normalized;
    }

    private String normalizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name is required");
        }

        String normalized = name.trim();
        if (normalized.length() > 100) {
            throw new IllegalArgumentException("name exceeds max length 100");
        }

        return normalized;
    }

    private String normalizeCountryCode(String countryCode) {
        if (countryCode == null || countryCode.trim().isEmpty()) {
            throw new IllegalArgumentException("countryCode is required");
        }

        String normalized = countryCode.trim().toUpperCase();
        if (normalized.length() != 3) {
            throw new IllegalArgumentException("countryCode must have length 3");
        }

        return normalized;
    }

    private boolean normalizeActive(Boolean active) {
        if (active == null) {
            throw new IllegalArgumentException("active is required");
        }

        return active;
    }
}
