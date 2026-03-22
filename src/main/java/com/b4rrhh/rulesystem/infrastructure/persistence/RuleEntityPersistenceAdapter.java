package com.b4rrhh.rulesystem.infrastructure.persistence;

import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
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
    public List<RuleEntity> findByFilters(
            String ruleSystemCode,
            String ruleEntityTypeCode,
            String code,
            Boolean active,
            LocalDate referenceDate
    ) {
        return springDataRuleEntityRepository.findByFilters(
                        ruleSystemCode,
                        ruleEntityTypeCode,
                        code,
                        active,
                        referenceDate,
                        SpringDataRuleEntityRepository.MAX_DATE
                )
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
    public Optional<RuleEntity> findByBusinessKeyAndStartDate(
            String ruleSystemCode,
            String ruleEntityTypeCode,
            String code,
            LocalDate startDate
    ) {
        return springDataRuleEntityRepository
                .findByRuleSystemCodeAndRuleEntityTypeCodeAndCodeAndStartDate(
                        ruleSystemCode,
                        ruleEntityTypeCode,
                        code,
                        startDate
                )
                .map(this::toDomain);
    }

    @Override
    public boolean existsOverlapExcludingStartDate(
            String ruleSystemCode,
            String ruleEntityTypeCode,
            String code,
            LocalDate projectedStartDate,
            LocalDate projectedEndDate,
            LocalDate excludedStartDate
    ) {
        return springDataRuleEntityRepository.existsOverlapExcludingStartDate(
                ruleSystemCode,
                ruleEntityTypeCode,
                code,
                projectedStartDate,
                projectedEndDate,
                excludedStartDate,
                SpringDataRuleEntityRepository.MAX_DATE
        );
    }

    @Override
    public void deleteByBusinessKeyAndStartDate(
            String ruleSystemCode,
            String ruleEntityTypeCode,
            String code,
            LocalDate startDate
    ) {
        springDataRuleEntityRepository.deleteByRuleSystemCodeAndRuleEntityTypeCodeAndCodeAndStartDate(
                ruleSystemCode,
                ruleEntityTypeCode,
                code,
                startDate
        );
    }

    @Override
    public RuleEntity save(RuleEntity ruleEntity) {
        RuleEntityEntity entity = ruleEntity.getId() == null
                ? toNewEntity(ruleEntity)
                : toExistingEntity(ruleEntity);
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

    private RuleEntityEntity toNewEntity(RuleEntity ruleEntity) {
        RuleEntityEntity entity = new RuleEntityEntity();
        entity.setRuleSystemCode(ruleEntity.getRuleSystemCode());
        entity.setRuleEntityTypeCode(ruleEntity.getRuleEntityTypeCode());
        entity.setCode(ruleEntity.getCode());
        entity.setStartDate(ruleEntity.getStartDate());
        applyMutableFields(entity, ruleEntity);
        return entity;
    }

    private RuleEntityEntity toExistingEntity(RuleEntity ruleEntity) {
        RuleEntityEntity entity = springDataRuleEntityRepository
                .findByRuleSystemCodeAndRuleEntityTypeCodeAndCodeAndStartDate(
                        ruleEntity.getRuleSystemCode(),
                        ruleEntity.getRuleEntityTypeCode(),
                        ruleEntity.getCode(),
                        ruleEntity.getStartDate()
                )
                .orElseThrow(() -> new IllegalStateException(
                        "Rule entity not found for update with business key: "
                                + ruleEntity.getRuleSystemCode()
                                + "/"
                                + ruleEntity.getRuleEntityTypeCode()
                                + "/"
                                + ruleEntity.getCode()
                                + "/"
                                + ruleEntity.getStartDate()
                ));

        applyMutableFields(entity, ruleEntity);
        return entity;
    }

    private void applyMutableFields(RuleEntityEntity entity, RuleEntity ruleEntity) {
        entity.setName(ruleEntity.getName());
        entity.setDescription(ruleEntity.getDescription());
        entity.setActive(ruleEntity.isActive());
        entity.setEndDate(ruleEntity.getEndDate());
    }
}
