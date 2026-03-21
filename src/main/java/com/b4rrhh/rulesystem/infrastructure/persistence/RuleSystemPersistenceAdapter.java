package com.b4rrhh.rulesystem.infrastructure.persistence;

import com.b4rrhh.rulesystem.domain.model.RuleSystem;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class RuleSystemPersistenceAdapter implements RuleSystemRepository {

    private final SpringDataRuleSystemRepository springDataRuleSystemRepository;

    public RuleSystemPersistenceAdapter(SpringDataRuleSystemRepository springDataRuleSystemRepository) {
        this.springDataRuleSystemRepository = springDataRuleSystemRepository;
    }

    @Override
    public Optional<RuleSystem> findByCode(String code) {
        return springDataRuleSystemRepository.findByCode(code).map(this::toDomain);
    }

    @Override
    public List<RuleSystem> findAll() {
        return springDataRuleSystemRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public RuleSystem save(RuleSystem ruleSystem) {
        RuleSystemEntity entity = toEntity(ruleSystem);
        RuleSystemEntity saved = springDataRuleSystemRepository.save(entity);
        return toDomain(saved);
    }

    private RuleSystem toDomain(RuleSystemEntity entity) {
        return new RuleSystem(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getCountryCode(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private RuleSystemEntity toEntity(RuleSystem ruleSystem) {
        RuleSystemEntity entity = new RuleSystemEntity();
        entity.setId(ruleSystem.getId());
        entity.setCode(ruleSystem.getCode());
        entity.setName(ruleSystem.getName());
        entity.setCountryCode(ruleSystem.getCountryCode());
        entity.setActive(ruleSystem.isActive());
        entity.setCreatedAt(ruleSystem.getCreatedAt());
        entity.setUpdatedAt(ruleSystem.getUpdatedAt());
        return entity;
    }
}