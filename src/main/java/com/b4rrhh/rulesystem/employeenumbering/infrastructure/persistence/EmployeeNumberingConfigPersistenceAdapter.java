package com.b4rrhh.rulesystem.employeenumbering.infrastructure.persistence;

import com.b4rrhh.rulesystem.employeenumbering.domain.model.EmployeeNumberingConfig;
import com.b4rrhh.rulesystem.employeenumbering.domain.port.EmployeeNumberingConfigRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class EmployeeNumberingConfigPersistenceAdapter
        implements EmployeeNumberingConfigRepository {

    private final SpringDataEmployeeNumberingConfigRepository springDataRepo;

    public EmployeeNumberingConfigPersistenceAdapter(
            SpringDataEmployeeNumberingConfigRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public Optional<EmployeeNumberingConfig> findByRuleSystemCode(String ruleSystemCode) {
        return springDataRepo.findByRuleSystemCode(ruleSystemCode).map(this::toDomain);
    }

    @Override
    public EmployeeNumberingConfig save(EmployeeNumberingConfig config) {
        EmployeeNumberingConfigEntity entity = springDataRepo
                .findByRuleSystemCode(config.ruleSystemCode())
                .orElseGet(EmployeeNumberingConfigEntity::new);

        entity.setRuleSystemCode(config.ruleSystemCode());
        entity.setPrefix(config.prefix());
        entity.setNumericPartLength(config.numericPartLength());
        entity.setStep(config.step());
        entity.setNextValue(config.nextValue());

        return toDomain(springDataRepo.save(entity));
    }

    private EmployeeNumberingConfig toDomain(EmployeeNumberingConfigEntity e) {
        return new EmployeeNumberingConfig(
                e.getRuleSystemCode(),
                e.getPrefix(),
                e.getNumericPartLength(),
                e.getStep(),
                e.getNextValue()
        );
    }
}
