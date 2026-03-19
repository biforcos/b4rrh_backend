package com.b4rrhh.employee.contract.application.usecase;

import com.b4rrhh.employee.contract.application.command.CloseContractCommand;
import com.b4rrhh.employee.contract.domain.model.Contract;

public interface CloseContractUseCase {

    Contract close(CloseContractCommand command);
}
