package com.b4rrhh.employee.address.application.usecase;

import com.b4rrhh.employee.address.domain.model.Address;

import java.util.List;

public interface ListEmployeeAddressesUseCase {

    List<Address> listByEmployeeBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );
}
