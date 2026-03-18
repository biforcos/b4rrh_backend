package com.b4rrhh.employee.cost_center.application.usecase;

import com.b4rrhh.employee.cost_center.application.command.CloseCostCenterCommand;
import com.b4rrhh.employee.cost_center.domain.model.CostCenterAllocation;

public interface CloseCostCenterUseCase {

    CostCenterAllocation close(CloseCostCenterCommand command);
}
