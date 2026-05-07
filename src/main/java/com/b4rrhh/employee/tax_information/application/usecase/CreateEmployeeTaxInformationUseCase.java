package com.b4rrhh.employee.tax_information.application.usecase;

import com.b4rrhh.employee.tax_information.domain.model.EmployeeTaxInformation;

public interface CreateEmployeeTaxInformationUseCase {
    EmployeeTaxInformation create(CreateEmployeeTaxInformationCommand command);
}
