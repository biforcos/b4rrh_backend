package com.b4rrhh.rulesystem.application.usecase;

import com.b4rrhh.rulesystem.domain.exception.RuleEntityNotFoundException;
import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class GetRuleEntityByBusinessKeyService implements GetRuleEntityByBusinessKeyUseCase {

    private final RuleEntityRepository ruleEntityRepository;

    public GetRuleEntityByBusinessKeyService(RuleEntityRepository ruleEntityRepository) {
        this.ruleEntityRepository = ruleEntityRepository;
    }

    @Override
    public RuleEntity get(GetRuleEntityByBusinessKeyQuery query) {
        String ruleSystemCode = normalizeRequiredCode("ruleSystemCode", query.ruleSystemCode());
        String ruleEntityTypeCode = normalizeRequiredCode("ruleEntityTypeCode", query.ruleEntityTypeCode());
        String code = normalizeRequiredCode("code", query.code());
        LocalDate startDate = normalizeRequiredStartDate(query.startDate());

        return ruleEntityRepository.findByBusinessKeyAndStartDate(ruleSystemCode, ruleEntityTypeCode, code, startDate)
                .orElseThrow(() -> new RuleEntityNotFoundException(ruleSystemCode, ruleEntityTypeCode, code, startDate));
    }

    private String normalizeRequiredCode(String fieldName, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        return value.trim().toUpperCase();
    }

    private LocalDate normalizeRequiredStartDate(LocalDate startDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("startDate is required");
        }

        return startDate;
    }
}
