package com.b4rrhh.employee.tax_information.application.usecase;

import com.b4rrhh.employee.tax_information.application.port.EmployeeForTaxInfoLookupPort;
import com.b4rrhh.employee.tax_information.domain.exception.EmployeeTaxInformationEmployeeNotFoundException;
import com.b4rrhh.employee.tax_information.domain.exception.EmployeeTaxInformationNotFoundException;
import com.b4rrhh.employee.tax_information.domain.port.EmployeeTaxInformationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteEmployeeTaxInformationService implements DeleteEmployeeTaxInformationUseCase {

    private final EmployeeTaxInformationRepository repo;
    private final EmployeeForTaxInfoLookupPort employeeLookupPort;

    public DeleteEmployeeTaxInformationService(EmployeeTaxInformationRepository repo,
            EmployeeForTaxInfoLookupPort employeeLookupPort) {
        this.repo = repo;
        this.employeeLookupPort = employeeLookupPort;
    }

    @Override
    @Transactional
    public void delete(DeleteEmployeeTaxInformationCommand cmd) {
        String rs = cmd.ruleSystemCode().trim().toUpperCase();
        String type = cmd.employeeTypeCode().trim().toUpperCase();
        String num = cmd.employeeNumber().trim();

        Long employeeId = employeeLookupPort.findEmployeeId(rs, type, num)
            .orElseThrow(() -> new EmployeeTaxInformationEmployeeNotFoundException(rs, type, num));

        if (repo.findByEmployeeIdAndValidFrom(employeeId, cmd.validFrom()).isEmpty()) {
            throw new EmployeeTaxInformationNotFoundException(employeeId, cmd.validFrom());
        }

        repo.deleteByEmployeeIdAndValidFrom(employeeId, cmd.validFrom());
    }
}
