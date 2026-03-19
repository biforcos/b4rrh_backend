package com.b4rrhh.employee.labor_classification.application.usecase;

import com.b4rrhh.employee.labor_classification.application.command.ListEmployeeLaborClassificationsCommand;
import com.b4rrhh.employee.labor_classification.domain.model.LaborClassification;

import java.util.List;

public interface ListEmployeeLaborClassificationsUseCase {

    List<LaborClassification> listByEmployeeBusinessKey(ListEmployeeLaborClassificationsCommand command);
}
