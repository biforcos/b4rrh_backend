package com.b4rrhh.employee.cost_center.application.usecase;

import com.b4rrhh.employee.cost_center.application.command.CreateCostCenterCommand;
import com.b4rrhh.employee.cost_center.domain.model.CostCenterAllocation;

public interface CreateCostCenterUseCase {

    CostCenterAllocation create(CreateCostCenterCommand command);
}
