package com.b4rrhh.employee.address.application.usecase;

import com.b4rrhh.employee.address.domain.model.Address;

import java.util.Optional;

public interface GetAddressByBusinessKeyUseCase {

    Optional<Address> getByBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            Integer addressNumber
    );
}
