package com.b4rrhh.rulesystem.employeedisplaynameformat.application.usecase;

import com.b4rrhh.rulesystem.employeedisplaynameformat.domain.model.EmployeeDisplayNameFormat;

import java.util.Optional;

public interface GetEmployeeDisplayNameFormatUseCase {
    Optional<EmployeeDisplayNameFormat> getByRuleSystemCode(String ruleSystemCode);
}
