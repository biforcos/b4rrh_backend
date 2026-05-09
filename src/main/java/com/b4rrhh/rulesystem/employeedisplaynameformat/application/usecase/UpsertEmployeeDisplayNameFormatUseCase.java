package com.b4rrhh.rulesystem.employeedisplaynameformat.application.usecase;

import com.b4rrhh.rulesystem.employeedisplaynameformat.domain.model.EmployeeDisplayNameFormat;

public interface UpsertEmployeeDisplayNameFormatUseCase {
    EmployeeDisplayNameFormat upsert(UpsertEmployeeDisplayNameFormatCommand command);
}
