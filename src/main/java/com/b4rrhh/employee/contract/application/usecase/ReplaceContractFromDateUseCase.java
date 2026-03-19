package com.b4rrhh.employee.contract.application.usecase;

import com.b4rrhh.employee.contract.application.command.ReplaceContractFromDateCommand;
import com.b4rrhh.employee.contract.domain.model.Contract;

public interface ReplaceContractFromDateUseCase {

    Contract replaceFromDate(ReplaceContractFromDateCommand command);
}
