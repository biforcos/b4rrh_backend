package com.b4rrhh.employee.cost_center.application.usecase;

public interface ListCostCenterDistributionHistoryUseCase {

    CostCenterDistributionReadModel.History listHistory(ListCostCenterDistributionHistoryQuery query);
}
