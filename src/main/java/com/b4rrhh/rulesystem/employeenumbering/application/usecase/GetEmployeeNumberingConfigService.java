package com.b4rrhh.rulesystem.employeenumbering.application.usecase;

import com.b4rrhh.rulesystem.employeenumbering.domain.model.EmployeeNumberingConfig;
import com.b4rrhh.rulesystem.employeenumbering.domain.port.EmployeeNumberingConfigRepository;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class GetEmployeeNumberingConfigService implements GetEmployeeNumberingConfigUseCase {

    private final EmployeeNumberingConfigRepository repository;

    public GetEmployeeNumberingConfigService(EmployeeNumberingConfigRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<EmployeeNumberingConfig> getByRuleSystemCode(String ruleSystemCode) {
        return repository.findByRuleSystemCode(ruleSystemCode.trim().toUpperCase(Locale.ROOT));
    }
}
