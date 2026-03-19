package com.b4rrhh.employee.labor_classification.application.usecase;

import com.b4rrhh.employee.labor_classification.application.command.GetLaborClassificationByBusinessKeyCommand;
import com.b4rrhh.employee.labor_classification.domain.model.LaborClassification;

public interface GetLaborClassificationByBusinessKeyUseCase {

    LaborClassification getByBusinessKey(GetLaborClassificationByBusinessKeyCommand command);
}
