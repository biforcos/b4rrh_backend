package com.b4rrhh.employee.tax_information.application.usecase;

import com.b4rrhh.employee.tax_information.application.port.EmployeeForTaxInfoLookupPort;
import com.b4rrhh.employee.tax_information.domain.exception.EmployeeTaxInformationEmployeeNotFoundException;
import com.b4rrhh.employee.tax_information.domain.exception.EmployeeTaxInformationNotFoundException;
import com.b4rrhh.employee.tax_information.domain.model.EmployeeTaxInformation;
import com.b4rrhh.employee.tax_information.domain.port.EmployeeTaxInformationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CorrectEmployeeTaxInformationService implements CorrectEmployeeTaxInformationUseCase {

    private final EmployeeTaxInformationRepository repo;
    private final EmployeeForTaxInfoLookupPort employeeLookupPort;

    public CorrectEmployeeTaxInformationService(EmployeeTaxInformationRepository repo,
            EmployeeForTaxInfoLookupPort employeeLookupPort) {
        this.repo = repo;
        this.employeeLookupPort = employeeLookupPort;
    }

    @Override
    @Transactional
    public EmployeeTaxInformation correct(CorrectEmployeeTaxInformationCommand cmd) {
        String rs = cmd.ruleSystemCode().trim().toUpperCase();
        String type = cmd.employeeTypeCode().trim().toUpperCase();
        String num = cmd.employeeNumber().trim();

        Long employeeId = employeeLookupPort.findEmployeeId(rs, type, num)
            .orElseThrow(() -> new EmployeeTaxInformationEmployeeNotFoundException(rs, type, num));

        EmployeeTaxInformation existing = repo.findByEmployeeIdAndValidFrom(employeeId, cmd.validFrom())
            .orElseThrow(() -> new EmployeeTaxInformationNotFoundException(employeeId, cmd.validFrom()));

        return repo.save(existing.correct(cmd.familySituation(), cmd.descendantsCount(),
            cmd.ascendantsCount(), cmd.disabilityDegree(), cmd.pensionCompensatoria(),
            cmd.geographicMobility(), cmd.habitualResidenceLoan(), cmd.taxTerritory()));
    }
}
