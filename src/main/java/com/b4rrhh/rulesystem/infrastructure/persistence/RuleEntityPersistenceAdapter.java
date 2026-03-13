package com.b4rrhh.rulesystem.infrastructure.persistence;

import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class RuleEntityPersistenceAdapter implements RuleEntityRepository {

    private final SpringDataRuleEntityRepository springDataRuleEntityRepository;

    public RuleEntityPersistenceAdapter(SpringDataRuleEntityRepository springDataRuleEntityRepository) {
        this.springDataRuleEntityRepository = springDataRuleEntityRepository;
    }

    @Override
    public List<RuleEntity> findAll() {
        return springDataRuleEntityRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<RuleEntity> findByFilters(String ruleSystemCode, String ruleEntityTypeCode, String code, Boolean active) {
        return springDataRuleEntityRepository.findByFilters(ruleSystemCode, ruleEntityTypeCode, code, active)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<RuleEntity> findByBusinessKey(String ruleSystemCode, String ruleEntityTypeCode, String code) {
        return springDataRuleEntityRepository
                .findByRuleSystemCodeAndRuleEntityTypeCodeAndCode(ruleSystemCode, ruleEntityTypeCode, code)
                .map(this::toDomain);
    }

    @Override
    public RuleEntity save(RuleEntity ruleEntity) {
        RuleEntityEntity entity = toEntity(ruleEntity);
        RuleEntityEntity saved = springDataRuleEntityRepository.save(entity);
        return toDomain(saved);
    }

    private RuleEntity toDomain(RuleEntityEntity entity) {
        return new RuleEntity(
                entity.getId(),
                entity.getRuleSystemCode(),
                entity.getRuleEntityTypeCode(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.isActive(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private RuleEntityEntity toEntity(RuleEntity ruleEntity) {
        RuleEntityEntity entity = new RuleEntityEntity();
        entity.setId(ruleEntity.getId());
        entity.setRuleSystemCode(ruleEntity.getRuleSystemCode());
        entity.setRuleEntityTypeCode(ruleEntity.getRuleEntityTypeCode());
        entity.setCode(ruleEntity.getCode());
        entity.setName(ruleEntity.getName());
        entity.setDescription(ruleEntity.getDescription());
        entity.setActive(ruleEntity.isActive());
        entity.setStartDate(ruleEntity.getStartDate());
        entity.setEndDate(ruleEntity.getEndDate());
        return entity;
    }
}
