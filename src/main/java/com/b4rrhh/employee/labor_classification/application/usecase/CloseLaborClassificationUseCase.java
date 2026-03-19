package com.b4rrhh.employee.labor_classification.application.usecase;

import com.b4rrhh.employee.labor_classification.application.command.CloseLaborClassificationCommand;
import com.b4rrhh.employee.labor_classification.domain.model.LaborClassification;

public interface CloseLaborClassificationUseCase {

    LaborClassification close(CloseLaborClassificationCommand command);
}
