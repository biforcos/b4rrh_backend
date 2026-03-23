package com.b4rrhh.employee.lifecycle.application.usecase;

import com.b4rrhh.employee.lifecycle.application.command.TerminateEmployeeCommand;
import com.b4rrhh.employee.lifecycle.application.model.TerminateEmployeeResult;

public interface TerminateEmployeeUseCase {

    TerminateEmployeeResult terminate(TerminateEmployeeCommand command);
}
