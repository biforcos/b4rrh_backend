package com.b4rrhh.employee.contract.application.usecase;

import com.b4rrhh.employee.contract.application.command.UpdateContractCommand;
import com.b4rrhh.employee.contract.domain.model.Contract;

public interface UpdateContractUseCase {

    Contract update(UpdateContractCommand command);
}
