package com.b4rrhh.rulesystem.employeenumbering.application.usecase;

import com.b4rrhh.rulesystem.employeenumbering.domain.model.EmployeeNumberingConfig;

public interface UpsertEmployeeNumberingConfigUseCase {
    EmployeeNumberingConfig upsert(UpsertEmployeeNumberingConfigCommand command);
}
