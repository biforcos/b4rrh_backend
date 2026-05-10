package com.b4rrhh.rulesystem.employeenumbering.application.usecase;

import com.b4rrhh.rulesystem.employeenumbering.domain.model.EmployeeNumberingConfig;

import java.util.Optional;

public interface GetEmployeeNumberingConfigUseCase {
    Optional<EmployeeNumberingConfig> getByRuleSystemCode(String ruleSystemCode);
}
