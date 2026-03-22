package com.b4rrhh.rulesystem.catalogoption.infrastructure.persistence;

import com.b4rrhh.rulesystem.catalogoption.domain.model.DirectCatalogOption;
import com.b4rrhh.rulesystem.catalogoption.domain.port.DirectCatalogOptionRepository;
import com.b4rrhh.rulesystem.infrastructure.persistence.RuleEntityEntity;
import com.b4rrhh.rulesystem.infrastructure.persistence.SpringDataRuleEntityRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class RuleEntityDirectCatalogOptionReadAdapter implements DirectCatalogOptionRepository {

    private final SpringDataRuleEntityRepository springDataRuleEntityRepository;

    public RuleEntityDirectCatalogOptionReadAdapter(SpringDataRuleEntityRepository springDataRuleEntityRepository) {
        this.springDataRuleEntityRepository = springDataRuleEntityRepository;
    }

    @Override
    public List<DirectCatalogOption> findDirectOptions(
            String ruleSystemCode,
            String ruleEntityTypeCode,
            LocalDate referenceDate,
            String qLike
    ) {
        return springDataRuleEntityRepository
                .findDirectCatalogOptions(
                        ruleSystemCode,
                        ruleEntityTypeCode,
                        referenceDate,
                        qLike,
                        SpringDataRuleEntityRepository.MAX_DATE
                )
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private DirectCatalogOption toDomain(RuleEntityEntity entity) {
        return new DirectCatalogOption(
                entity.getCode(),
                entity.getName(),
                entity.isActive(),
                entity.getStartDate(),
                entity.getEndDate()
        );
    }
}
