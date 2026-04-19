package com.b4rrhh.payroll_engine.object.infrastructure.persistence;

import com.b4rrhh.payroll_engine.object.domain.model.PayrollObject;
import com.b4rrhh.payroll_engine.object.domain.model.PayrollObjectTypeCode;
import com.b4rrhh.payroll_engine.object.domain.port.PayrollObjectRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PayrollObjectPersistenceAdapter implements PayrollObjectRepository {

    private final SpringDataPayrollObjectRepository springDataRepository;

    public PayrollObjectPersistenceAdapter(SpringDataPayrollObjectRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public PayrollObject save(PayrollObject payrollObject) {
        PayrollObjectEntity entity = toEntity(payrollObject);
        PayrollObjectEntity saved = springDataRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<PayrollObject> findByBusinessKey(
            String ruleSystemCode,
            PayrollObjectTypeCode objectTypeCode,
            String objectCode
    ) {
        return springDataRepository
                .findByRuleSystemCodeAndObjectTypeCodeAndObjectCode(
                        ruleSystemCode,
                        objectTypeCode.name(),
                        objectCode
                )
                .map(this::toDomain);
    }

    @Override
    public boolean existsByBusinessKey(
            String ruleSystemCode,
            PayrollObjectTypeCode objectTypeCode,
            String objectCode
    ) {
        return springDataRepository.existsByRuleSystemCodeAndObjectTypeCodeAndObjectCode(
                ruleSystemCode,
                objectTypeCode.name(),
                objectCode
        );
    }

    private PayrollObjectEntity toEntity(PayrollObject domain) {
        PayrollObjectEntity entity = new PayrollObjectEntity();
        entity.setRuleSystemCode(domain.getRuleSystemCode());
        entity.setObjectTypeCode(domain.getObjectTypeCode().name());
        entity.setObjectCode(domain.getObjectCode());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    private PayrollObject toDomain(PayrollObjectEntity entity) {
        return new PayrollObject(
                entity.getId(),
                entity.getRuleSystemCode(),
                PayrollObjectTypeCode.valueOf(entity.getObjectTypeCode()),
                entity.getObjectCode(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
