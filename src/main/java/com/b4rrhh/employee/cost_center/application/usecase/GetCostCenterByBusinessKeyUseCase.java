package com.b4rrhh.employee.cost_center.application.usecase;

import com.b4rrhh.employee.cost_center.application.command.GetCostCenterByBusinessKeyCommand;
import com.b4rrhh.employee.cost_center.domain.model.CostCenterAllocation;

public interface GetCostCenterByBusinessKeyUseCase {

    CostCenterAllocation getByBusinessKey(GetCostCenterByBusinessKeyCommand command);
}
