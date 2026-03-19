package com.b4rrhh.employee.contract.application.usecase;

import com.b4rrhh.employee.contract.application.command.ListEmployeeContractsCommand;
import com.b4rrhh.employee.contract.domain.model.Contract;

import java.util.List;

public interface ListEmployeeContractsUseCase {

    List<Contract> listByEmployeeBusinessKey(ListEmployeeContractsCommand command);
}
