package com.b4rrhh.employee.labor_classification.application.usecase;

import com.b4rrhh.employee.labor_classification.application.command.UpdateLaborClassificationCommand;
import com.b4rrhh.employee.labor_classification.domain.model.LaborClassification;

public interface UpdateLaborClassificationUseCase {

    LaborClassification update(UpdateLaborClassificationCommand command);
}
