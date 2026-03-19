package com.b4rrhh.employee.contract.application.usecase;

import com.b4rrhh.employee.contract.application.command.GetContractByBusinessKeyCommand;
import com.b4rrhh.employee.contract.domain.model.Contract;

public interface GetContractByBusinessKeyUseCase {

    Contract getByBusinessKey(GetContractByBusinessKeyCommand command);
}
