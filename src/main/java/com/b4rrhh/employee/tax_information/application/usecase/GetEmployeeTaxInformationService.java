package com.b4rrhh.employee.tax_information.application.usecase;

import com.b4rrhh.employee.tax_information.application.port.EmployeeForTaxInfoLookupPort;
import com.b4rrhh.employee.tax_information.domain.exception.EmployeeTaxInformationEmployeeNotFoundException;
import com.b4rrhh.employee.tax_information.domain.exception.EmployeeTaxInformationNotFoundException;
import com.b4rrhh.employee.tax_information.domain.model.EmployeeTaxInformation;
import com.b4rrhh.employee.tax_information.domain.port.EmployeeTaxInformationRepository;
import org.springframework.stereotype.Service;

@Service
public class GetEmployeeTaxInformationService implements GetEmployeeTaxInformationUseCase {

    private final EmployeeTaxInformationRepository repo;
    private final EmployeeForTaxInfoLookupPort employeeLookupPort;

    public GetEmployeeTaxInformationService(EmployeeTaxInformationRepository repo,
            EmployeeForTaxInfoLookupPort employeeLookupPort) {
        this.repo = repo;
        this.employeeLookupPort = employeeLookupPort;
    }

    @Override
    public EmployeeTaxInformation get(GetEmployeeTaxInformationCommand cmd) {
        String rs = cmd.ruleSystemCode().trim().toUpperCase();
        String type = cmd.employeeTypeCode().trim().toUpperCase();
        String num = cmd.employeeNumber().trim();

        Long employeeId = employeeLookupPort.findEmployeeId(rs, type, num)
            .orElseThrow(() -> new EmployeeTaxInformationEmployeeNotFoundException(rs, type, num));

        return repo.findByEmployeeIdAndValidFrom(employeeId, cmd.validFrom())
            .orElseThrow(() -> new EmployeeTaxInformationNotFoundException(employeeId, cmd.validFrom()));
    }
}
