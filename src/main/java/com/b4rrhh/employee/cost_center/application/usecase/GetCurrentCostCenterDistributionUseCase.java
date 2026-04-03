package com.b4rrhh.employee.cost_center.application.usecase;

public interface GetCurrentCostCenterDistributionUseCase {

    CostCenterDistributionReadModel.CurrentDistribution getCurrent(GetCurrentCostCenterDistributionQuery query);
}
