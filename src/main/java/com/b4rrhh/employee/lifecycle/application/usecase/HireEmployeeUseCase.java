package com.b4rrhh.employee.lifecycle.application.usecase;

import com.b4rrhh.employee.lifecycle.application.command.HireEmployeeCommand;
import com.b4rrhh.employee.lifecycle.application.model.HireEmployeeResult;

public interface HireEmployeeUseCase {

    HireEmployeeResult hire(HireEmployeeCommand command);
}
