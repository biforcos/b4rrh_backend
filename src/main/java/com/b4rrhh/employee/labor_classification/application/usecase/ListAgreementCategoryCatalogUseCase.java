package com.b4rrhh.employee.labor_classification.application.usecase;

import com.b4rrhh.employee.labor_classification.application.command.ListAgreementCategoryCatalogCommand;
import com.b4rrhh.employee.labor_classification.application.model.AgreementCategoryCatalogItem;

import java.util.List;

public interface ListAgreementCategoryCatalogUseCase {

    List<AgreementCategoryCatalogItem> list(ListAgreementCategoryCatalogCommand command);
}
