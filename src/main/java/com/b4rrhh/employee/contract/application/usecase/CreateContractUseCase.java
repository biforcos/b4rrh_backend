package com.b4rrhh.employee.contract.application.usecase;

import com.b4rrhh.employee.contract.application.command.CreateContractCommand;
import com.b4rrhh.employee.contract.domain.model.Contract;

public interface CreateContractUseCase {

    Contract create(CreateContractCommand command);
}
