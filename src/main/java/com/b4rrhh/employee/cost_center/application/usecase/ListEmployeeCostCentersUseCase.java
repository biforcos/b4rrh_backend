package com.b4rrhh.employee.cost_center.application.usecase;

import com.b4rrhh.employee.cost_center.application.command.ListEmployeeCostCentersCommand;
import com.b4rrhh.employee.cost_center.domain.model.CostCenterAllocation;

import java.util.List;

public interface ListEmployeeCostCentersUseCase {

    List<CostCenterAllocation> listByEmployeeBusinessKey(ListEmployeeCostCentersCommand command);
}
