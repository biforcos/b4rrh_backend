package com.b4rrhh.employee.tax_information.application.usecase;

import com.b4rrhh.employee.tax_information.domain.model.EmployeeTaxInformation;
import java.util.List;

public interface ListEmployeeTaxInformationUseCase {
    List<EmployeeTaxInformation> list(ListEmployeeTaxInformationCommand command);
}
