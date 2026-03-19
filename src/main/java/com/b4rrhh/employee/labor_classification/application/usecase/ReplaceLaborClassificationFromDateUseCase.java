package com.b4rrhh.employee.labor_classification.application.usecase;

import com.b4rrhh.employee.labor_classification.application.command.ReplaceLaborClassificationFromDateCommand;
import com.b4rrhh.employee.labor_classification.domain.model.LaborClassification;

public interface ReplaceLaborClassificationFromDateUseCase {

    LaborClassification replaceFromDate(ReplaceLaborClassificationFromDateCommand command);
}
