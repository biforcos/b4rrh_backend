package com.b4rrhh.employee.contract.application.usecase;

import com.b4rrhh.employee.contract.application.command.ListContractSubtypeCatalogCommand;
import com.b4rrhh.employee.contract.application.model.ContractSubtypeCatalogItem;

import java.util.List;

public interface ListContractSubtypeCatalogUseCase {

    List<ContractSubtypeCatalogItem> list(ListContractSubtypeCatalogCommand command);
}