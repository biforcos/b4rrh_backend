package com.b4rrhh.rulesystem.employeedisplaynameformat.infrastructure.persistence;

import com.b4rrhh.rulesystem.employeedisplaynameformat.domain.model.DisplayNameFormatCode;
import com.b4rrhh.rulesystem.employeedisplaynameformat.domain.model.EmployeeDisplayNameFormat;
import com.b4rrhh.rulesystem.employeedisplaynameformat.domain.port.EmployeeDisplayNameFormatRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class EmployeeDisplayNameFormatPersistenceAdapter
        implements EmployeeDisplayNameFormatRepository {

    private final SpringDataEmployeeDisplayNameFormatRepository springDataRepo;

    public EmployeeDisplayNameFormatPersistenceAdapter(
            SpringDataEmployeeDisplayNameFormatRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public Optional<EmployeeDisplayNameFormat> findByRuleSystemCode(String ruleSystemCode) {
        return springDataRepo.findByRuleSystemCode(ruleSystemCode)
                .map(this::toDomain);
    }

    @Override
    public EmployeeDisplayNameFormat save(EmployeeDisplayNameFormat format) {
        EmployeeDisplayNameFormatEntity entity = springDataRepo
                .findByRuleSystemCode(format.ruleSystemCode())
                .orElseGet(EmployeeDisplayNameFormatEntity::new);

        entity.setRuleSystemCode(format.ruleSystemCode());
        entity.setDisplayNameFormatCode(format.formatCode().name());

        return toDomain(springDataRepo.save(entity));
    }

    private EmployeeDisplayNameFormat toDomain(EmployeeDisplayNameFormatEntity entity) {
        return new EmployeeDisplayNameFormat(
                entity.getRuleSystemCode(),
                DisplayNameFormatCode.valueOf(entity.getDisplayNameFormatCode())
        );
    }
}
