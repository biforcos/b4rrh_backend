package com.b4rrhh.employee.address.application.usecase;

import com.b4rrhh.employee.address.domain.model.Address;

public interface CreateAddressUseCase {

    Address create(CreateAddressCommand command);
}
