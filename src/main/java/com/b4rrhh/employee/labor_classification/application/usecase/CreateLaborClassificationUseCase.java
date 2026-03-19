package com.b4rrhh.employee.labor_classification.application.usecase;

import com.b4rrhh.employee.labor_classification.application.command.CreateLaborClassificationCommand;
import com.b4rrhh.employee.labor_classification.domain.model.LaborClassification;

public interface CreateLaborClassificationUseCase {

    LaborClassification create(CreateLaborClassificationCommand command);
}
