package com.b4rrhh.employee.tax_information.application.usecase;

import com.b4rrhh.employee.tax_information.domain.model.EmployeeTaxInformation;

public interface GetEmployeeTaxInformationUseCase {
    EmployeeTaxInformation get(GetEmployeeTaxInformationCommand command);
}
