package com.b4rrhh.employee.identifier.application.usecase;

import com.b4rrhh.employee.identifier.domain.model.Identifier;

import java.util.List;

public interface ListEmployeeIdentifiersUseCase {

    List<Identifier> listByEmployeeBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );
}
