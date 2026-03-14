package com.b4rrhh.employee.identifier.application.usecase;

import com.b4rrhh.employee.identifier.domain.model.Identifier;

import java.util.Optional;

public interface GetIdentifierByBusinessKeyUseCase {

    Optional<Identifier> getByBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            String identifierTypeCode
    );
}
