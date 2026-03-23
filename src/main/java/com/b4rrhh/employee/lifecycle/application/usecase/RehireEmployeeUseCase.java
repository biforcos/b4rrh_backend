package com.b4rrhh.employee.lifecycle.application.usecase;

import com.b4rrhh.employee.lifecycle.application.command.RehireEmployeeCommand;
import com.b4rrhh.employee.lifecycle.application.model.RehireEmployeeResult;

public interface RehireEmployeeUseCase {

    RehireEmployeeResult rehire(RehireEmployeeCommand command);
}