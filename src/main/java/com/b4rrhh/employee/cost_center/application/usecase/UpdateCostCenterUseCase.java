package com.b4rrhh.employee.cost_center.application.usecase;

import com.b4rrhh.employee.cost_center.application.command.UpdateCostCenterCommand;
import com.b4rrhh.employee.cost_center.domain.model.CostCenterAllocation;

public interface UpdateCostCenterUseCase {

    CostCenterAllocation update(UpdateCostCenterCommand command);
}
